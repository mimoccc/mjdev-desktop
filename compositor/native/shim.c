/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 *
 * wlroots 0.18 based compositor core, exposed to Kotlin/Native
 * through the flat API in shim.h. Policy (layers, focus order,
 * IPC) lives on the Kotlin side.
 */

#define _POSIX_C_SOURCE 200809L

#include <assert.h>
#include <signal.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <time.h>
#include <unistd.h>

#include <wayland-server-core.h>
#include <wlr/backend.h>
#include <wlr/backend/session.h>
#include <wlr/render/allocator.h>
#include <wlr/render/wlr_renderer.h>
#include <wlr/render/gles2.h>
#include <wlr/render/egl.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <wlr/types/wlr_compositor.h>
#include <wlr/types/wlr_subcompositor.h>
#include <wlr/types/wlr_cursor.h>
#include <wlr/types/wlr_data_device.h>
#include <wlr/types/wlr_input_device.h>
#include <wlr/types/wlr_keyboard.h>
#include <wlr/types/wlr_output.h>
#include <wlr/types/wlr_output_layout.h>
#include <wlr/types/wlr_pointer.h>
#include <wlr/types/wlr_scene.h>
#include <wlr/types/wlr_seat.h>
#include <wlr/types/wlr_xcursor_manager.h>
#include <wlr/types/wlr_xdg_decoration_v1.h>
#include <wlr/types/wlr_xdg_shell.h>
#include <wlr/util/log.h>
#include <wlr/xwayland.h>
#include <xkbcommon/xkbcommon.h>

/* server-side window decorations: rendered with cairo into an image surface that
 * is wrapped in a custom wlr_buffer and shown via a wlr_scene_buffer node */
#include <math.h>
#include <cairo.h>
#include <drm_fourcc.h>
#include <wlr/interfaces/wlr_buffer.h>

/* _POSIX_C_SOURCE hides the XOPEN/GNU math constants from <math.h> */
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

#include "shim.h"

/* GNOME-style decoration geometry (logical px) */
#define MJC_DECO_TITLEBAR_H 36
#define MJC_DECO_RADIUS     10
#define MJC_DECO_BORDER     3
#define MJC_DECO_BTN_R      9   /* button circle radius */
#define MJC_DECO_BTN_GAP    8   /* gap between button circles */
#define MJC_DECO_PAD        14  /* horizontal padding (title left, buttons right) */
#define MJC_DECO_RESIZE     6   /* edge/corner resize hotzone half-width (px) */

enum mjc_cursor_mode {
    MJC_CURSOR_PASSTHROUGH,
    MJC_CURSOR_MOVE,
    MJC_CURSOR_RESIZE,
};

struct mjc_server {
    struct wl_display *display;
    struct wl_event_loop *loop;
    struct wlr_backend *backend;
    struct wlr_session *session;
    struct wlr_renderer *renderer;
    struct wlr_allocator *allocator;
    struct wlr_compositor *compositor;
    struct wlr_scene *scene;
    struct wlr_scene_output_layout *scene_layout;
    struct wlr_output_layout *output_layout;
    struct wlr_scene_tree *layers[5];

    struct wlr_xdg_shell *xdg_shell;
    struct wlr_xdg_decoration_manager_v1 *xdg_decoration_mgr;
    struct wlr_xwayland *xwayland;

    struct wlr_cursor *cursor;
    struct wlr_xcursor_manager *cursor_mgr;
    struct wlr_seat *seat;

    struct wl_list views;     /* mjc_view.link */
    struct wl_list keyboards; /* mjc_keyboard.link */
    struct wl_list fd_sources;/* mjc_fd_source.link */

    struct mjc_view *focused_view;
    mjc_callbacks cbs;
    void *ud;
    const char *socket;
    uint64_t next_view_id;

    /* nested-output size (16:9 default); used when the backend has no mode list
     * (wayland/headless) and to lock the window against host-driven resizes */
    int output_w, output_h;
    /* throttle timestamp (msec) for the pointer callback */
    uint32_t last_pointer_ms;

    /* injection feedback marker (Android "show touches" style): a small white square in
     * the overlay layer that appears at the injected pointer and fades out after a timeout */
    struct wlr_scene_rect *inject_marker;
    struct wl_event_source *inject_fade_timer;
    float inject_alpha;

    enum mjc_cursor_mode cursor_mode;
    struct mjc_view *grabbed_view;
    double grab_x, grab_y;
    struct wlr_box grab_geobox;
    uint32_t resize_edges;

    struct wl_event_source *sigchld_source;

    /* pids spawned via mjc_spawn; sigchld must reap only these,
     * other children (Xwayland) belong to wlroots */
    pid_t spawned_pids[64];
    int spawned_count;

    /* server-side decoration theme (straight-alpha colors, 0..1) derived from the
     * shell background; the compositor paints every normal-layer window's frame with it */
    float deco_bg[3];
    float deco_fg[3];

    struct wl_listener new_output;
    struct wl_listener new_xdg_toplevel;
    struct wl_listener new_xdg_popup;
    struct wl_listener new_toplevel_decoration;
    struct wl_listener new_xwayland_surface;
    struct wl_listener xwayland_ready;
    struct wl_listener cursor_motion;
    struct wl_listener cursor_motion_absolute;
    struct wl_listener cursor_button;
    struct wl_listener cursor_axis;
    struct wl_listener cursor_frame;
    struct wl_listener new_input;
    struct wl_listener request_cursor;
    struct wl_listener request_set_selection;
};

struct mjc_view {
    struct wl_list link;
    struct mjc_server *server;
    uint64_t id;
    bool is_xwayland;
    bool mapped;
    bool minimized;
    bool maximized;
    /* re-center once the client commits its post-unmaximize size */
    bool pending_center;
    bool focusable;
    mjc_layer layer;
    struct wlr_scene_tree *scene_tree;
    /* floating geometry remembered while maximized (x/y are scene coords) */
    struct wlr_box saved_geo;

    /* server-side decoration (only for normal-layer app windows). The titlebar is a
     * cairo buffer node, the borders are scene rects; all are children of scene_tree so
     * they move/raise/minimize with the window automatically. */
    bool decorated;
    bool deco_disabled;                    /* chromeless: never draw a server frame */
    bool is_chrome;                         /* shell surface (wallpaper/panel/menu): no frame */
    struct wlr_scene_buffer *deco_titlebar;
    struct wlr_scene_rect *deco_border[3]; /* left, right, bottom */
    int deco_w, deco_h;                    /* last rendered content size */
    int hovered_button;                    /* -1 none, 0 min, 1 max, 2 close */

    /* xdg shell */
    struct wlr_xdg_toplevel *xdg_toplevel;

    /* xwayland */
    struct wlr_xwayland_surface *xsurface;

    struct wl_listener map;
    struct wl_listener unmap;
    struct wl_listener commit;
    struct wl_listener destroy;
    struct wl_listener request_move;
    struct wl_listener request_resize;
    struct wl_listener request_maximize;
    struct wl_listener request_minimize;
    struct wl_listener request_fullscreen;
    struct wl_listener set_title;
    struct wl_listener set_app_id; /* set_class for xwayland */
    struct wl_listener associate;
    struct wl_listener dissociate;
    struct wl_listener request_configure;
    struct wl_listener set_geometry;
};

struct mjc_popup {
    struct wlr_xdg_popup *xdg_popup;
    struct wl_listener commit;
    struct wl_listener destroy;
};

struct mjc_decoration {
    struct wlr_xdg_toplevel_decoration_v1 *decoration;
    struct mjc_view *view;
    struct wl_listener request_mode;
    struct wl_listener destroy;
};

struct mjc_keyboard {
    struct wl_list link;
    struct mjc_server *server;
    struct wlr_keyboard *wlr_keyboard;
    struct wl_listener modifiers;
    struct wl_listener key;
    struct wl_listener destroy;
};

struct mjc_output {
    struct wl_list link;
    struct mjc_server *server;
    struct wlr_output *wlr_output;
    struct wl_listener frame;
    struct wl_listener request_state;
    struct wl_listener destroy;
};

struct mjc_fd_source {
    struct wl_list link;
    struct mjc_server *server;
    int fd;
    struct wl_event_source *source;
};

/* ------------------------------------------------------------------ */
/* helpers                                                             */
/* ------------------------------------------------------------------ */

static struct wlr_surface *view_surface(struct mjc_view *view) {
    if (view->is_xwayland) {
        return view->xsurface != NULL ? view->xsurface->surface : NULL;
    }
    return view->xdg_toplevel->base->surface;
}

/* re-render a window's titlebar bitmap in place (focus/hover/title changes) */
static void deco_repaint(struct mjc_view *view);

static void emit_focus_change(struct mjc_server *server) {
    if (server->cbs.focus_change != NULL) {
        server->cbs.focus_change(server->ud, server->focused_view);
    }
}

static void view_deactivate(struct mjc_view *view) {
    if (view->is_xwayland) {
        if (view->xsurface != NULL) {
            wlr_xwayland_surface_activate(view->xsurface, false);
        }
    } else {
        wlr_xdg_toplevel_set_activated(view->xdg_toplevel, false);
    }
}

static void view_focus_internal(struct mjc_view *view, bool raise) {
    if (view == NULL || !view->focusable || view->minimized) {
        return;
    }
    struct mjc_server *server = view->server;
    struct wlr_seat *seat = server->seat;
    if (server->focused_view == view) {
        return;
    }
    struct mjc_view *prev = server->focused_view;
    if (server->focused_view != NULL) {
        view_deactivate(server->focused_view);
    }
    /* focus-follows-mouse must not restack windows (no autoraise); only an explicit
     * focus (click / activate / unminimize) raises the view to the top */
    if (raise && view->scene_tree != NULL) {
        wlr_scene_node_raise_to_top(&view->scene_tree->node);
    }
    if (view->is_xwayland) {
        wlr_xwayland_surface_activate(view->xsurface, true);
        wlr_xwayland_surface_restack(view->xsurface, NULL, XCB_STACK_MODE_ABOVE);
    } else {
        wlr_xdg_toplevel_set_activated(view->xdg_toplevel, true);
    }
    struct wlr_surface *surface = view_surface(view);
    struct wlr_keyboard *keyboard = wlr_seat_get_keyboard(seat);
    if (surface != NULL) {
        if (keyboard != NULL) {
            wlr_seat_keyboard_notify_enter(seat, surface,
                keyboard->keycodes, keyboard->num_keycodes, &keyboard->modifiers);
        } else {
            wlr_seat_keyboard_notify_enter(seat, surface, NULL, 0, NULL);
        }
    }
    server->focused_view = view;
    /* refresh active/inactive titlebar styling on both windows */
    if (prev != NULL) {
        deco_repaint(prev);
    }
    deco_repaint(view);
    emit_focus_change(server);
}

void mjc_view_focus(struct mjc_view *view) {
    view_focus_internal(view, true);
}

static void clear_focus_if(struct mjc_server *server, struct mjc_view *view) {
    if (server->focused_view == view) {
        server->focused_view = NULL;
        emit_focus_change(server);
    }
    if (server->grabbed_view == view) {
        server->grabbed_view = NULL;
        server->cursor_mode = MJC_CURSOR_PASSTHROUGH;
    }
}

static struct mjc_view *desktop_view_at(struct mjc_server *server,
        double lx, double ly, struct wlr_surface **surface,
        double *sx, double *sy) {
    struct wlr_scene_node *node =
        wlr_scene_node_at(&server->scene->tree.node, lx, ly, sx, sy);
    if (node == NULL || node->type != WLR_SCENE_NODE_BUFFER) {
        return NULL;
    }
    struct wlr_scene_buffer *scene_buffer = wlr_scene_buffer_from_node(node);
    struct wlr_scene_surface *scene_surface =
        wlr_scene_surface_try_from_buffer(scene_buffer);
    if (scene_surface == NULL) {
        return NULL;
    }
    *surface = scene_surface->surface;
    struct wlr_scene_tree *tree = node->parent;
    while (tree != NULL && tree->node.data == NULL) {
        tree = tree->node.parent;
    }
    return tree != NULL ? tree->node.data : NULL;
}

/* ------------------------------------------------------------------ */
/* outputs                                                             */
/* ------------------------------------------------------------------ */

static void output_frame(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_output *output = wl_container_of(listener, output, frame);
    struct wlr_scene_output *scene_output = wlr_scene_get_scene_output(
        output->server->scene, output->wlr_output);
    if (scene_output == NULL) {
        return;
    }
    wlr_scene_output_commit(scene_output, NULL);
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    wlr_scene_output_send_frame_done(scene_output, &now);
}

static void output_request_state(struct wl_listener *listener, void *data) {
    struct mjc_output *output = wl_container_of(listener, output, request_state);
    const struct wlr_output_event_request_state *event = data;
    /* honor host-driven resizes (the user resizing the nested x11/wayland window) so the
     * desktop relayouts to the new size; commit the requested state and remember the new
     * dimensions, which the shell reads back via the wl_output geometry */
    wlr_output_commit_state(output->wlr_output, event->state);
    output->server->output_w = output->wlr_output->width;
    output->server->output_h = output->wlr_output->height;
}

static void output_destroy(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_output *output = wl_container_of(listener, output, destroy);
    struct mjc_server *server = output->server;
    wl_list_remove(&output->frame.link);
    wl_list_remove(&output->request_state.link);
    wl_list_remove(&output->destroy.link);
    wl_list_remove(&output->link);
    free(output);
    /* nested backends (x11/wayland) have no session; closing the host window
     * destroys the output, so quit the compositor and let the run action finish.
     * a real (drm) session keeps running when an output is unplugged. */
    if (server->session == NULL) {
        wl_display_terminate(server->display);
    }
}

static void server_new_output(struct wl_listener *listener, void *data) {
    struct mjc_server *server = wl_container_of(listener, server, new_output);
    struct wlr_output *wlr_output = data;

    wlr_output_init_render(wlr_output, server->allocator, server->renderer);

    struct wlr_output_state state;
    wlr_output_state_init(&state);
    wlr_output_state_set_enabled(&state, true);
    struct wlr_output_mode *mode = wlr_output_preferred_mode(wlr_output);
    if (mode != NULL) {
        wlr_output_state_set_mode(&state, mode);
    } else {
        /* wayland / headless backends expose no mode list, so preferred_mode is
         * NULL; without a mode the output has no dimensions and renders black.
         * pin it to the configured 16:9 size. */
        wlr_output_state_set_custom_mode(&state,
            server->output_w, server->output_h, 0);
    }
    wlr_output_commit_state(wlr_output, &state);
    wlr_output_state_finish(&state);

    struct mjc_output *output = calloc(1, sizeof(*output));
    output->wlr_output = wlr_output;
    output->server = server;

    output->frame.notify = output_frame;
    wl_signal_add(&wlr_output->events.frame, &output->frame);
    output->request_state.notify = output_request_state;
    wl_signal_add(&wlr_output->events.request_state, &output->request_state);
    output->destroy.notify = output_destroy;
    wl_signal_add(&wlr_output->events.destroy, &output->destroy);

    wl_list_init(&output->link);

    struct wlr_output_layout_output *l_output =
        wlr_output_layout_add_auto(server->output_layout, wlr_output);
    struct wlr_scene_output *scene_output =
        wlr_scene_output_create(server->scene, wlr_output);
    wlr_scene_output_layout_add_output(server->scene_layout, l_output, scene_output);
}

/* ------------------------------------------------------------------ */
/* keyboard & input                                                    */
/* ------------------------------------------------------------------ */

static void keyboard_handle_modifiers(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_keyboard *keyboard = wl_container_of(listener, keyboard, modifiers);
    wlr_seat_set_keyboard(keyboard->server->seat, keyboard->wlr_keyboard);
    wlr_seat_keyboard_notify_modifiers(keyboard->server->seat,
        &keyboard->wlr_keyboard->modifiers);
}

static void keyboard_handle_key(struct wl_listener *listener, void *data) {
    struct mjc_keyboard *keyboard = wl_container_of(listener, keyboard, key);
    struct mjc_server *server = keyboard->server;
    struct wlr_keyboard_key_event *event = data;

    uint32_t keycode = event->keycode + 8;
    const xkb_keysym_t *syms;
    int nsyms = xkb_state_key_get_syms(
        keyboard->wlr_keyboard->xkb_state, keycode, &syms);
    uint32_t modifiers = wlr_keyboard_get_modifiers(keyboard->wlr_keyboard);
    bool pressed = event->state == WL_KEYBOARD_KEY_STATE_PRESSED;

    bool handled = false;
    for (int i = 0; i < nsyms; i++) {
        /* VT switching always works */
        if (pressed && syms[i] >= XKB_KEY_XF86Switch_VT_1 &&
                syms[i] <= XKB_KEY_XF86Switch_VT_12) {
            if (server->session != NULL) {
                wlr_session_change_vt(server->session,
                    syms[i] - XKB_KEY_XF86Switch_VT_1 + 1);
            }
            handled = true;
            continue;
        }
        if (server->cbs.key != NULL &&
                server->cbs.key(server->ud, syms[i], modifiers, pressed)) {
            handled = true;
        }
    }

    if (!handled) {
        wlr_seat_set_keyboard(server->seat, keyboard->wlr_keyboard);
        wlr_seat_keyboard_notify_key(server->seat,
            event->time_msec, event->keycode, event->state);
    }
}

static void keyboard_handle_destroy(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_keyboard *keyboard = wl_container_of(listener, keyboard, destroy);
    wl_list_remove(&keyboard->modifiers.link);
    wl_list_remove(&keyboard->key.link);
    wl_list_remove(&keyboard->destroy.link);
    wl_list_remove(&keyboard->link);
    free(keyboard);
}

static void server_new_keyboard(struct mjc_server *server,
        struct wlr_input_device *device) {
    struct wlr_keyboard *wlr_keyboard = wlr_keyboard_from_input_device(device);

    struct mjc_keyboard *keyboard = calloc(1, sizeof(*keyboard));
    keyboard->server = server;
    keyboard->wlr_keyboard = wlr_keyboard;

    struct xkb_context *context = xkb_context_new(XKB_CONTEXT_NO_FLAGS);
    struct xkb_keymap *keymap = xkb_keymap_new_from_names(context, NULL,
        XKB_KEYMAP_COMPILE_NO_FLAGS);
    wlr_keyboard_set_keymap(wlr_keyboard, keymap);
    xkb_keymap_unref(keymap);
    xkb_context_unref(context);
    wlr_keyboard_set_repeat_info(wlr_keyboard, 25, 600);

    keyboard->modifiers.notify = keyboard_handle_modifiers;
    wl_signal_add(&wlr_keyboard->events.modifiers, &keyboard->modifiers);
    keyboard->key.notify = keyboard_handle_key;
    wl_signal_add(&wlr_keyboard->events.key, &keyboard->key);
    keyboard->destroy.notify = keyboard_handle_destroy;
    wl_signal_add(&device->events.destroy, &keyboard->destroy);

    wlr_seat_set_keyboard(server->seat, wlr_keyboard);
    wl_list_insert(&server->keyboards, &keyboard->link);
}

static void server_new_pointer(struct mjc_server *server,
        struct wlr_input_device *device) {
    wlr_cursor_attach_input_device(server->cursor, device);
}

static void server_new_input(struct wl_listener *listener, void *data) {
    struct mjc_server *server = wl_container_of(listener, server, new_input);
    struct wlr_input_device *device = data;
    switch (device->type) {
    case WLR_INPUT_DEVICE_KEYBOARD:
        server_new_keyboard(server, device);
        break;
    case WLR_INPUT_DEVICE_POINTER:
        server_new_pointer(server, device);
        break;
    default:
        break;
    }
    uint32_t caps = WL_SEAT_CAPABILITY_POINTER;
    if (!wl_list_empty(&server->keyboards)) {
        caps |= WL_SEAT_CAPABILITY_KEYBOARD;
    }
    wlr_seat_set_capabilities(server->seat, caps);
}

static void seat_request_cursor(struct wl_listener *listener, void *data) {
    struct mjc_server *server = wl_container_of(listener, server, request_cursor);
    struct wlr_seat_pointer_request_set_cursor_event *event = data;
    struct wlr_seat_client *focused_client =
        server->seat->pointer_state.focused_client;
    if (focused_client == event->seat_client) {
        wlr_cursor_set_surface(server->cursor, event->surface,
            event->hotspot_x, event->hotspot_y);
    }
}

static void seat_request_set_selection(struct wl_listener *listener, void *data) {
    struct mjc_server *server =
        wl_container_of(listener, server, request_set_selection);
    struct wlr_seat_request_set_selection_event *event = data;
    wlr_seat_set_selection(server->seat, event->source, event->serial);
}

/* ------------------------------------------------------------------ */
/* cursor                                                              */
/* ------------------------------------------------------------------ */

static void view_current_geometry(struct mjc_view *view, struct wlr_box *box);

/* server-side decoration helpers (defined after the interactive-grab section) */
static void view_decoration_update(struct mjc_view *view);
static void view_decoration_destroy(struct mjc_view *view);
/* returns true when the press/release was consumed by a window's decoration */
static bool deco_handle_button(struct mjc_server *server,
    double lx, double ly, bool pressed);
/* recompute which titlebar button (if any) the pointer hovers, re-rendering on change */
static void deco_update_hover(struct mjc_server *server);
/* layout-space top-left and width of a decorated view's titlebar; false if undecorated */
static bool deco_titlebar_box(struct mjc_view *view,
    double *tx, double *ty, int *w);
/* resize-edge mask under (lx,ly) for a decorated window (0 = not on an edge) */
static uint32_t deco_resize_edges(struct mjc_server *server,
    double lx, double ly, struct mjc_view **out);
/* xcursor name for a resize-edge mask */
static const char *deco_resize_cursor(uint32_t edges);

static void process_cursor_move(struct mjc_server *server) {
    struct mjc_view *view = server->grabbed_view;
    if (view == NULL || view->scene_tree == NULL) {
        return;
    }
    int x = (int)(server->cursor->x - server->grab_x);
    int y = (int)(server->cursor->y - server->grab_y);
    wlr_scene_node_set_position(&view->scene_tree->node, x, y);
    if (view->is_xwayland && view->xsurface != NULL) {
        wlr_xwayland_surface_configure(view->xsurface,
            (int16_t) x, (int16_t) y,
            (uint16_t) view->xsurface->width, (uint16_t) view->xsurface->height);
    }
}

static void process_cursor_resize(struct mjc_server *server) {
    struct mjc_view *view = server->grabbed_view;
    if (view == NULL || view->scene_tree == NULL) {
        return;
    }
    double border_x = server->cursor->x - server->grab_x;
    double border_y = server->cursor->y - server->grab_y;
    int new_left = server->grab_geobox.x;
    int new_right = server->grab_geobox.x + server->grab_geobox.width;
    int new_top = server->grab_geobox.y;
    int new_bottom = server->grab_geobox.y + server->grab_geobox.height;

    if (server->resize_edges & WLR_EDGE_TOP) {
        new_top = (int) border_y;
        if (new_top >= new_bottom) {
            new_top = new_bottom - 1;
        }
    } else if (server->resize_edges & WLR_EDGE_BOTTOM) {
        new_bottom = (int) border_y;
        if (new_bottom <= new_top) {
            new_bottom = new_top + 1;
        }
    }
    if (server->resize_edges & WLR_EDGE_LEFT) {
        new_left = (int) border_x;
        if (new_left >= new_right) {
            new_left = new_right - 1;
        }
    } else if (server->resize_edges & WLR_EDGE_RIGHT) {
        new_right = (int) border_x;
        if (new_right <= new_left) {
            new_right = new_left + 1;
        }
    }

    int new_width = new_right - new_left;
    int new_height = new_bottom - new_top;

    if (view->is_xwayland && view->xsurface != NULL) {
        wlr_scene_node_set_position(&view->scene_tree->node, new_left, new_top);
        wlr_xwayland_surface_configure(view->xsurface,
            (int16_t) new_left, (int16_t) new_top,
            (uint16_t) new_width, (uint16_t) new_height);
    } else {
        struct wlr_box geo_box;
        wlr_xdg_surface_get_geometry(view->xdg_toplevel->base, &geo_box);
        wlr_scene_node_set_position(&view->scene_tree->node,
            new_left - geo_box.x, new_top - geo_box.y);
        wlr_xdg_toplevel_set_size(view->xdg_toplevel, new_width, new_height);
    }
}

/* 1 = focus-follows-mouse (focusable view under the pointer gets focus). The focus-clear
 * over the desktop is intentionally NOT done here: it made the dock reveal jam after a couple
 * of cycles. The dock autohide must instead be driven by pointer-leave on the shell side. */
#define MJC_FOCUS_FOLLOWS_MOUSE 1

static void process_cursor_motion(struct mjc_server *server, uint32_t time) {
    if (server->cursor_mode == MJC_CURSOR_MOVE) {
        process_cursor_move(server);
        return;
    } else if (server->cursor_mode == MJC_CURSOR_RESIZE) {
        process_cursor_resize(server);
        return;
    }

    double sx, sy;
    struct wlr_seat *seat = server->seat;
    struct wlr_surface *surface = NULL;
    struct mjc_view *view = desktop_view_at(server, server->cursor->x,
        server->cursor->y, &surface, &sx, &sy);

    /* server-side decorations own the pointer over the frame: refresh button hover
     * and pick the cursor (resize on edges/corners, hand over buttons, arrow over the
     * draggable titlebar). When the frame owns the pointer the client gets no focus. */
    deco_update_hover(server);
    struct mjc_view *rview = NULL;
    uint32_t redges = deco_resize_edges(server, server->cursor->x,
        server->cursor->y, &rview);
    bool deco_owns = false;
    const char *deco_cursor = NULL;
    if (redges != 0) {
        deco_cursor = deco_resize_cursor(redges);
        deco_owns = true;
    } else {
        struct mjc_view *tv;
        wl_list_for_each(tv, &server->views, link) {
            double tx, ty;
            int w;
            if (!deco_titlebar_box(tv, &tx, &ty, &w)) {
                continue;
            }
            if (server->cursor->x >= tx && server->cursor->x < tx + w &&
                    server->cursor->y >= ty &&
                    server->cursor->y < ty + MJC_DECO_TITLEBAR_H) {
                deco_cursor = (tv->hovered_button >= 0) ? "pointer" : "default";
                deco_owns = true;
                break;
            }
        }
    }

    if (deco_owns) {
        wlr_cursor_set_xcursor(server->cursor, server->cursor_mgr, deco_cursor);
        wlr_seat_pointer_clear_focus(seat);
    } else if (surface == NULL) {
        wlr_cursor_set_xcursor(server->cursor, server->cursor_mgr, "default");
        wlr_seat_pointer_clear_focus(seat);
    } else {
        wlr_seat_pointer_notify_enter(seat, surface, sx, sy);
        wlr_seat_pointer_notify_motion(seat, time, sx, sy);
        /* focus-follows-mouse: focusable view under the pointer gets keyboard focus (no
         * restack). Do NOT clear focus over non-focusable surfaces — that churns focus and
         * jams the dock reveal; autohide is handled by pointer-leave on the shell side. */
        if (MJC_FOCUS_FOLLOWS_MOUSE && view != NULL && view->focusable) {
            view_focus_internal(view, false);
        }
    }

    // broadcast the pointer position to ipc clients (throttled), so the shell can do
    // edge detection without the AWT global pointer which is unreliable under nested XWayland
    if (server->cbs.pointer != NULL && (time - server->last_pointer_ms) >= 30u) {
        server->last_pointer_ms = time;
        server->cbs.pointer(server->ud,
            (int) server->cursor->x, (int) server->cursor->y);
    }
}

static void server_cursor_motion(struct wl_listener *listener, void *data) {
    struct mjc_server *server = wl_container_of(listener, server, cursor_motion);
    struct wlr_pointer_motion_event *event = data;
    wlr_cursor_move(server->cursor, &event->pointer->base,
        event->delta_x, event->delta_y);
    process_cursor_motion(server, event->time_msec);
}

static void server_cursor_motion_absolute(struct wl_listener *listener, void *data) {
    struct mjc_server *server =
        wl_container_of(listener, server, cursor_motion_absolute);
    struct wlr_pointer_motion_absolute_event *event = data;
    wlr_cursor_warp_absolute(server->cursor, &event->pointer->base,
        event->x, event->y);
    process_cursor_motion(server, event->time_msec);
}

static void server_cursor_button(struct wl_listener *listener, void *data) {
    struct mjc_server *server = wl_container_of(listener, server, cursor_button);
    struct wlr_pointer_button_event *event = data;
    wlr_seat_pointer_notify_button(server->seat,
        event->time_msec, event->button, event->state);

    if (event->state == WL_POINTER_BUTTON_STATE_RELEASED) {
        server->cursor_mode = MJC_CURSOR_PASSTHROUGH;
        server->grabbed_view = NULL;
    } else {
        /* a press over a window's titlebar/buttons is handled by the decoration
         * (focus + move grab, or a min/max/close action) and must not fall through
         * to client focus */
        if (deco_handle_button(server, server->cursor->x, server->cursor->y, true)) {
            return;
        }
        double sx, sy;
        struct wlr_surface *surface = NULL;
        struct mjc_view *view = desktop_view_at(server,
            server->cursor->x, server->cursor->y, &surface, &sx, &sy);
        if (view != NULL && view->focusable) {
            mjc_view_focus(view);
        }
    }
}

static void server_cursor_axis(struct wl_listener *listener, void *data) {
    struct mjc_server *server = wl_container_of(listener, server, cursor_axis);
    struct wlr_pointer_axis_event *event = data;
    wlr_seat_pointer_notify_axis(server->seat,
        event->time_msec, event->orientation, event->delta,
        event->delta_discrete, event->source, event->relative_direction);
}

static void server_cursor_frame(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_server *server = wl_container_of(listener, server, cursor_frame);
    wlr_seat_pointer_notify_frame(server->seat);
}

/* ------------------------------------------------------------------ */
/* interactive move/resize                                             */
/* ------------------------------------------------------------------ */

static void view_current_geometry(struct mjc_view *view, struct wlr_box *box) {
    if (view->is_xwayland && view->xsurface != NULL) {
        box->x = view->scene_tree != NULL ? view->scene_tree->node.x : view->xsurface->x;
        box->y = view->scene_tree != NULL ? view->scene_tree->node.y : view->xsurface->y;
        box->width = view->xsurface->width;
        box->height = view->xsurface->height;
    } else {
        struct wlr_box geo;
        wlr_xdg_surface_get_geometry(view->xdg_toplevel->base, &geo);
        box->x = view->scene_tree->node.x + geo.x;
        box->y = view->scene_tree->node.y + geo.y;
        box->width = geo.width;
        box->height = geo.height;
    }
}

static void begin_interactive(struct mjc_view *view,
        enum mjc_cursor_mode mode, uint32_t edges) {
    struct mjc_server *server = view->server;
    if (view_surface(view) !=
            server->seat->pointer_state.focused_surface) {
        /* only react to requests from the focused client */
        return;
    }
    server->grabbed_view = view;
    server->cursor_mode = mode;

    struct wlr_box geo_box;
    view_current_geometry(view, &geo_box);

    if (mode == MJC_CURSOR_MOVE) {
        server->grab_x = server->cursor->x - view->scene_tree->node.x;
        server->grab_y = server->cursor->y - view->scene_tree->node.y;
    } else {
        double border_x = geo_box.x +
            ((edges & WLR_EDGE_RIGHT) ? geo_box.width : 0);
        double border_y = geo_box.y +
            ((edges & WLR_EDGE_BOTTOM) ? geo_box.height : 0);
        server->grab_x = server->cursor->x - border_x;
        server->grab_y = server->cursor->y - border_y;
        server->grab_geobox = geo_box;
        server->resize_edges = edges;
    }
}

/* ------------------------------------------------------------------ */
/* server-side decorations                                             */
/* ------------------------------------------------------------------ */

/* a wlr_buffer backed by a cairo ARGB32 image surface. cairo ARGB32 is
 * premultiplied and laid out as B,G,R,A on little-endian, which matches
 * DRM_FORMAT_ARGB8888 — so the scene can upload it directly as a texture. */
struct mjc_cairo_buffer {
    struct wlr_buffer base;
    cairo_surface_t *surface;
};

static void cairo_buffer_destroy(struct wlr_buffer *wlr_buffer) {
    struct mjc_cairo_buffer *buf = wl_container_of(wlr_buffer, buf, base);
    cairo_surface_destroy(buf->surface);
    free(buf);
}

static bool cairo_buffer_begin_data_ptr_access(struct wlr_buffer *wlr_buffer,
        uint32_t flags, void **data, uint32_t *format, size_t *stride) {
    struct mjc_cairo_buffer *buf = wl_container_of(wlr_buffer, buf, base);
    if (flags & WLR_BUFFER_DATA_PTR_ACCESS_WRITE) {
        return false;
    }
    *data = cairo_image_surface_get_data(buf->surface);
    *format = DRM_FORMAT_ARGB8888;
    *stride = (size_t) cairo_image_surface_get_stride(buf->surface);
    return true;
}

static void cairo_buffer_end_data_ptr_access(struct wlr_buffer *wlr_buffer) {
    (void) wlr_buffer;
}

static const struct wlr_buffer_impl cairo_buffer_impl = {
    .destroy = cairo_buffer_destroy,
    .begin_data_ptr_access = cairo_buffer_begin_data_ptr_access,
    .end_data_ptr_access = cairo_buffer_end_data_ptr_access,
};

static struct mjc_cairo_buffer *cairo_buffer_create(int width, int height) {
    struct mjc_cairo_buffer *buf = calloc(1, sizeof(*buf));
    if (buf == NULL) {
        return NULL;
    }
    buf->surface = cairo_image_surface_create(CAIRO_FORMAT_ARGB32, width, height);
    if (cairo_surface_status(buf->surface) != CAIRO_STATUS_SUCCESS) {
        cairo_surface_destroy(buf->surface);
        free(buf);
        return NULL;
    }
    wlr_buffer_init(&buf->base, &cairo_buffer_impl, width, height);
    return buf;
}

/* content-surface offset inside the view's scene_tree (xdg windows carry a
 * geometry offset for their CSD margins; xwayland surfaces sit at 0,0) */
static void view_geo_offset(struct mjc_view *view, int *ox, int *oy) {
    if (view->is_xwayland) {
        *ox = 0;
        *oy = 0;
    } else {
        struct wlr_box geo;
        wlr_xdg_surface_get_geometry(view->xdg_toplevel->base, &geo);
        *ox = geo.x;
        *oy = geo.y;
    }
}

static void view_content_size(struct mjc_view *view, int *w, int *h) {
    struct wlr_box box;
    view_current_geometry(view, &box);
    *w = box.width;
    *h = box.height;
}

/* center x of titlebar button i (0=min, 1=max, 2=close), right-aligned GNOME order */
static int deco_button_cx(int width, int i) {
    int step = 2 * MJC_DECO_BTN_R + MJC_DECO_BTN_GAP;
    int close_cx = width - MJC_DECO_PAD - MJC_DECO_BTN_R;
    return close_cx - (2 - i) * step;
}

static bool view_should_decorate(struct mjc_view *view) {
    if (!view->mapped || view->scene_tree == NULL) {
        return false;
    }
    /* chrome (shell wallpaper/panels/menus) and chromeless windows get no frame;
     * everything else is a normal app window and is decorated in any layer, so
     * always-on-top / always-on-bottom apps keep their titlebar */
    if (view->is_chrome || view->deco_disabled) {
        return false;
    }
    if (view->is_xwayland && view->xsurface != NULL &&
            view->xsurface->override_redirect) {
        return false;
    }
    return true;
}

/* a path with rounded top corners and square bottom corners */
static void deco_rounded_top(cairo_t *cr, double x, double y,
        double w, double h, double r) {
    cairo_new_sub_path(cr);
    cairo_arc(cr, x + r, y + r, r, M_PI, 1.5 * M_PI);
    cairo_arc(cr, x + w - r, y + r, r, 1.5 * M_PI, 2.0 * M_PI);
    cairo_line_to(cr, x + w, y + h);
    cairo_line_to(cr, x, y + h);
    cairo_close_path(cr);
}

/* render the titlebar of `width` px into a fresh cairo buffer and hand it to the
 * scene node (the scene takes a ref; we drop ours) */
static void deco_render(struct mjc_view *view, int width) {
    if (view->deco_titlebar == NULL || width <= 0) {
        return;
    }
    struct mjc_server *server = view->server;
    int h = MJC_DECO_TITLEBAR_H;
    struct mjc_cairo_buffer *buf = cairo_buffer_create(width, h);
    if (buf == NULL) {
        return;
    }
    cairo_t *cr = cairo_create(buf->surface);

    cairo_set_operator(cr, CAIRO_OPERATOR_CLEAR);
    cairo_paint(cr);
    cairo_set_operator(cr, CAIRO_OPERATOR_OVER);

    const float *bg = server->deco_bg;
    const float *fg = server->deco_fg;
    bool active = (server->focused_view == view);
    double dim = active ? 0.0 : 0.10;

    /* titlebar fill: soft vertical gradient derived from the theme bg */
    cairo_pattern_t *grad = cairo_pattern_create_linear(0, 0, 0, h);
    cairo_pattern_add_color_stop_rgb(grad, 0.0,
        fmin(bg[0] + 0.10 - dim, 1.0), fmin(bg[1] + 0.10 - dim, 1.0),
        fmin(bg[2] + 0.10 - dim, 1.0));
    cairo_pattern_add_color_stop_rgb(grad, 1.0,
        fmax(bg[0] - dim, 0.0), fmax(bg[1] - dim, 0.0), fmax(bg[2] - dim, 0.0));
    deco_rounded_top(cr, 0, 0, width, h, MJC_DECO_RADIUS);
    cairo_set_source(cr, grad);
    cairo_fill(cr);
    cairo_pattern_destroy(grad);

    /* hairline top highlight for depth */
    cairo_set_source_rgba(cr, 1, 1, 1, active ? 0.10 : 0.05);
    cairo_set_line_width(cr, 1.0);
    deco_rounded_top(cr, 0.5, 0.5, width - 1, h, MJC_DECO_RADIUS);
    cairo_stroke(cr);

    /* title text (cairo toy font api, no pango dependency) */
    const char *title = mjc_view_title(view);
    if (title != NULL && title[0] != '\0') {
        cairo_select_font_face(cr, "Sans",
            CAIRO_FONT_SLANT_NORMAL, CAIRO_FONT_WEIGHT_BOLD);
        cairo_set_font_size(cr, 13.0);
        cairo_font_extents_t fe;
        cairo_font_extents(cr, &fe);
        double clip_r = deco_button_cx(width, 0) - MJC_DECO_BTN_R - 8;
        cairo_save(cr);
        cairo_rectangle(cr, 0, 0, clip_r > 0 ? clip_r : 0, h);
        cairo_clip(cr);
        cairo_set_source_rgba(cr, fg[0], fg[1], fg[2], active ? 0.95 : 0.6);
        cairo_move_to(cr, MJC_DECO_PAD, (h + fe.ascent - fe.descent) / 2.0);
        cairo_show_text(cr, title);
        cairo_restore(cr);
    }

    /* three GNOME-style circular buttons: min, max, close */
    double cy = h / 2.0;
    for (int i = 0; i < 3; i++) {
        double cx = deco_button_cx(width, i);
        bool hov = (view->hovered_button == i);
        bool is_close = (i == 2);

        if (is_close && hov) {
            cairo_set_source_rgba(cr, 0.86, 0.22, 0.20, 1.0);
        } else {
            double a = hov ? 0.22 : 0.12;
            cairo_set_source_rgba(cr, fg[0], fg[1], fg[2], active ? a : a * 0.6);
        }
        cairo_arc(cr, cx, cy, MJC_DECO_BTN_R, 0, 2 * M_PI);
        cairo_fill(cr);

        if (is_close && hov) {
            cairo_set_source_rgba(cr, 1, 1, 1, 1.0);
        } else {
            cairo_set_source_rgba(cr, fg[0], fg[1], fg[2], active ? 0.92 : 0.55);
        }
        cairo_set_line_width(cr, 1.4);
        double g = 4.0;
        if (i == 0) {            /* minimize: bottom dash */
            cairo_move_to(cr, cx - g, cy + 2.5);
            cairo_line_to(cr, cx + g, cy + 2.5);
            cairo_stroke(cr);
        } else if (i == 1) {     /* maximize: square */
            cairo_rectangle(cr, cx - g, cy - g, 2 * g, 2 * g);
            cairo_stroke(cr);
        } else {                 /* close: x */
            cairo_move_to(cr, cx - g, cy - g);
            cairo_line_to(cr, cx + g, cy + g);
            cairo_move_to(cr, cx + g, cy - g);
            cairo_line_to(cr, cx - g, cy + g);
            cairo_stroke(cr);
        }
    }

    cairo_destroy(cr);
    cairo_surface_flush(buf->surface);

    wlr_scene_buffer_set_buffer(view->deco_titlebar, &buf->base);
    wlr_buffer_drop(&buf->base);
}

static void deco_repaint(struct mjc_view *view) {
    if (view->decorated && view->deco_w > 0) {
        deco_render(view, view->deco_w);
    }
}

static void view_decoration_destroy(struct mjc_view *view) {
    if (view->deco_titlebar != NULL) {
        wlr_scene_node_destroy(&view->deco_titlebar->node);
        view->deco_titlebar = NULL;
    }
    for (int i = 0; i < 3; i++) {
        if (view->deco_border[i] != NULL) {
            wlr_scene_node_destroy(&view->deco_border[i]->node);
            view->deco_border[i] = NULL;
        }
    }
    view->decorated = false;
    view->deco_w = 0;
    view->deco_h = 0;
    view->hovered_button = -1;
}

/* create/refresh/remove a window's decoration to match its current state */
static void view_decoration_update(struct mjc_view *view) {
    if (!view_should_decorate(view)) {
        view_decoration_destroy(view);
        return;
    }
    int w, h, ox, oy;
    view_content_size(view, &w, &h);
    view_geo_offset(view, &ox, &oy);
    if (w <= 0 || h <= 0) {
        return;
    }
    struct mjc_server *server = view->server;
    int tb = MJC_DECO_TITLEBAR_H;
    int b = MJC_DECO_BORDER;

    if (view->deco_titlebar == NULL) {
        view->deco_titlebar = wlr_scene_buffer_create(view->scene_tree, NULL);
        const float border_col[4] = {
            server->deco_bg[0] * 0.6f, server->deco_bg[1] * 0.6f,
            server->deco_bg[2] * 0.6f, 1.0f,
        };
        for (int i = 0; i < 3; i++) {
            view->deco_border[i] =
                wlr_scene_rect_create(view->scene_tree, 1, 1, border_col);
        }
        view->hovered_button = -1;
        view->decorated = true;
        view->deco_w = 0;
    }

    if (w != view->deco_w) {
        deco_render(view, w);
    }
    view->deco_w = w;
    view->deco_h = h;

    /* place relative to the content surface inside scene_tree; the titlebar sits
     * directly above the content, the borders frame the sides and bottom */
    wlr_scene_node_set_position(&view->deco_titlebar->node, ox, oy - tb);
    wlr_scene_rect_set_size(view->deco_border[0], b, tb + h);
    wlr_scene_node_set_position(&view->deco_border[0]->node, ox - b, oy - tb);
    wlr_scene_rect_set_size(view->deco_border[1], b, tb + h);
    wlr_scene_node_set_position(&view->deco_border[1]->node, ox + w, oy - tb);
    wlr_scene_rect_set_size(view->deco_border[2], w + 2 * b, b);
    wlr_scene_node_set_position(&view->deco_border[2]->node, ox - b, oy + h);
}

/* layout-space top-left and size of a decorated view's titlebar */
static bool deco_titlebar_box(struct mjc_view *view,
        double *tx, double *ty, int *w) {
    if (!view->decorated || view->deco_titlebar == NULL) {
        return false;
    }
    int ox, oy, h;
    view_geo_offset(view, &ox, &oy);
    view_content_size(view, w, &h);
    *tx = view->scene_tree->node.x + ox;
    *ty = view->scene_tree->node.y + oy - MJC_DECO_TITLEBAR_H;
    return true;
}

/* button index (0..2) under (lx,ly), or -1 if over the titlebar but not a button */
static int deco_button_index(double tx, double ty, int w, double lx, double ly) {
    double cy = ty + MJC_DECO_TITLEBAR_H / 2.0;
    for (int i = 0; i < 3; i++) {
        double cx = tx + deco_button_cx(w, i);
        double dx = lx - cx, dy = ly - cy;
        double rr = (MJC_DECO_BTN_R + 2) * (MJC_DECO_BTN_R + 2);
        if (dx * dx + dy * dy <= rr) {
            return i;
        }
    }
    return -1;
}

/* resize-edge mask under (lx,ly) for the first decorated, non-maximized window
 * whose frame border zone the pointer is in; 0 when the pointer is not on an edge */
static uint32_t deco_resize_edges(struct mjc_server *server,
        double lx, double ly, struct mjc_view **out) {
    struct mjc_view *view;
    wl_list_for_each(view, &server->views, link) {
        if (!view->decorated || view->scene_tree == NULL || view->maximized) {
            continue;
        }
        int ox, oy, w, h;
        view_geo_offset(view, &ox, &oy);
        view_content_size(view, &w, &h);
        double x0 = view->scene_tree->node.x + ox - MJC_DECO_BORDER;
        double y0 = view->scene_tree->node.y + oy - MJC_DECO_TITLEBAR_H;
        double x1 = view->scene_tree->node.x + ox + w + MJC_DECO_BORDER;
        double y1 = view->scene_tree->node.y + oy + h + MJC_DECO_BORDER;
        double r = MJC_DECO_RESIZE;
        if (lx < x0 - r || lx > x1 + r || ly < y0 - r || ly > y1 + r) {
            continue;
        }
        uint32_t edges = 0;
        if (ly <= y0 + r) {
            edges |= WLR_EDGE_TOP;
        } else if (ly >= y1 - r) {
            edges |= WLR_EDGE_BOTTOM;
        }
        if (lx <= x0 + r) {
            edges |= WLR_EDGE_LEFT;
        } else if (lx >= x1 - r) {
            edges |= WLR_EDGE_RIGHT;
        }
        if (edges != 0) {
            if (out != NULL) {
                *out = view;
            }
            return edges;
        }
    }
    return 0;
}

static const char *deco_resize_cursor(uint32_t edges) {
    bool top = edges & WLR_EDGE_TOP, bottom = edges & WLR_EDGE_BOTTOM;
    bool left = edges & WLR_EDGE_LEFT, right = edges & WLR_EDGE_RIGHT;
    if (top && left) return "nw-resize";
    if (top && right) return "ne-resize";
    if (bottom && left) return "sw-resize";
    if (bottom && right) return "se-resize";
    if (top) return "n-resize";
    if (bottom) return "s-resize";
    if (left) return "w-resize";
    if (right) return "e-resize";
    return "default";
}

static void deco_update_hover(struct mjc_server *server) {
    struct mjc_view *view;
    wl_list_for_each(view, &server->views, link) {
        double tx, ty;
        int w;
        if (!deco_titlebar_box(view, &tx, &ty, &w)) {
            continue;
        }
        int want = -1;
        if (server->cursor->x >= tx && server->cursor->x < tx + w &&
                server->cursor->y >= ty &&
                server->cursor->y < ty + MJC_DECO_TITLEBAR_H) {
            want = deco_button_index(tx, ty, w, server->cursor->x, server->cursor->y);
        }
        if (want != view->hovered_button) {
            view->hovered_button = want;
            deco_repaint(view);
        }
    }
}

static bool deco_handle_button(struct mjc_server *server,
        double lx, double ly, bool pressed) {
    if (!pressed) {
        return false;
    }

    /* edge/corner of a window frame => start an interactive resize. Mirrors the
     * RESIZE branch of begin_interactive but without the focused-surface guard
     * (the pointer is over the decoration, not the client surface). */
    struct mjc_view *rv = NULL;
    uint32_t redges = deco_resize_edges(server, lx, ly, &rv);
    if (redges != 0 && rv != NULL) {
        if (rv->focusable) {
            mjc_view_focus(rv);
        }
        struct wlr_box geo_box;
        view_current_geometry(rv, &geo_box);
        double border_x = geo_box.x +
            ((redges & WLR_EDGE_RIGHT) ? geo_box.width : 0);
        double border_y = geo_box.y +
            ((redges & WLR_EDGE_BOTTOM) ? geo_box.height : 0);
        server->grabbed_view = rv;
        server->cursor_mode = MJC_CURSOR_RESIZE;
        server->grab_x = server->cursor->x - border_x;
        server->grab_y = server->cursor->y - border_y;
        server->grab_geobox = geo_box;
        server->resize_edges = redges;
        return true;
    }

    struct mjc_view *view;
    wl_list_for_each(view, &server->views, link) {
        double tx, ty;
        int w;
        if (!deco_titlebar_box(view, &tx, &ty, &w)) {
            continue;
        }
        if (lx < tx || lx >= tx + w || ly < ty ||
                ly >= ty + MJC_DECO_TITLEBAR_H) {
            continue;
        }
        int btn = deco_button_index(tx, ty, w, lx, ly);
        if (view->focusable) {
            mjc_view_focus(view);
        }
        if (btn == 0) {
            mjc_view_set_minimized(view, true);
        } else if (btn == 1) {
            mjc_view_set_maximized(view, !view->maximized);
        } else if (btn == 2) {
            mjc_view_close(view);
        } else {
            /* drag the titlebar => interactive move (no focused-surface guard
             * because the pointer is over the decoration, not the client) */
            server->grabbed_view = view;
            server->cursor_mode = MJC_CURSOR_MOVE;
            server->grab_x = server->cursor->x - view->scene_tree->node.x;
            server->grab_y = server->cursor->y - view->scene_tree->node.y;
        }
        return true;
    }
    return false;
}

/* ------------------------------------------------------------------ */
/* view common                                                         */
/* ------------------------------------------------------------------ */

/* places a freshly mapped floating view in the middle of the output */
static void view_center(struct mjc_view *view) {
    struct mjc_server *server = view->server;
    if (view->scene_tree == NULL || view->maximized) {
        return;
    }
    struct wlr_box out;
    wlr_output_layout_get_box(server->output_layout, NULL, &out);
    struct wlr_box geo;
    view_current_geometry(view, &geo);
    if (out.width <= 0 || geo.width <= 0) {
        return;
    }
    int x = out.x + (out.width - geo.width) / 2;
    int y = out.y + (out.height - geo.height) / 2;
    if (x < out.x) x = out.x;
    if (y < out.y) y = out.y;
    if (view->is_xwayland) {
        mjc_view_set_position(view, x, y);
    } else {
        struct wlr_box off;
        wlr_xdg_surface_get_geometry(view->xdg_toplevel->base, &off);
        wlr_scene_node_set_position(&view->scene_tree->node, x - off.x, y - off.y);
    }
}

static void view_emit_new(struct mjc_view *view) {
    if (view->server->cbs.view_new != NULL) {
        view->server->cbs.view_new(view->server->ud, view, view->is_xwayland);
    }
}

static void view_emit_map(struct mjc_view *view) {
    if (view->server->cbs.view_map != NULL) {
        view->server->cbs.view_map(view->server->ud, view);
    }
}

static void view_emit_unmap(struct mjc_view *view) {
    if (view->server->cbs.view_unmap != NULL) {
        view->server->cbs.view_unmap(view->server->ud, view);
    }
}

static void view_emit_destroy(struct mjc_view *view) {
    if (view->server->cbs.view_destroy != NULL) {
        view->server->cbs.view_destroy(view->server->ud, view);
    }
}

static struct mjc_view *view_create(struct mjc_server *server, bool is_xwayland) {
    struct mjc_view *view = calloc(1, sizeof(*view));
    view->server = server;
    view->id = ++server->next_view_id;
    view->is_xwayland = is_xwayland;
    view->focusable = true;
    view->layer = MJC_LAYER_NORMAL;
    wl_list_insert(&server->views, &view->link);
    return view;
}

static void view_free(struct mjc_view *view) {
    clear_focus_if(view->server, view);
    view_emit_destroy(view);
    wl_list_remove(&view->link);
    free(view);
}

/* ------------------------------------------------------------------ */
/* xdg shell views                                                     */
/* ------------------------------------------------------------------ */

static void xdg_toplevel_map(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, map);
    view->mapped = true;
    view_center(view);
    view_emit_map(view);    /* policy assigns the layer here (synchronous) */
    mjc_view_focus(view);
    view_decoration_update(view);
}

static void xdg_toplevel_unmap(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, unmap);
    view->mapped = false;
    clear_focus_if(view->server, view);
    view_decoration_destroy(view);
    view_emit_unmap(view);
}

static void xdg_toplevel_commit(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, commit);
    if (view->xdg_toplevel->base->initial_commit) {
        if (view->xdg_toplevel->requested.maximized) {
            mjc_view_set_maximized(view, true);
        } else {
            /* 0x0 lets the client pick its own (or remembered) size */
            wlr_xdg_toplevel_set_size(view->xdg_toplevel, 0, 0);
        }
        return;
    }
    if (view->pending_center && view->mapped && !view->maximized &&
            !view->xdg_toplevel->current.maximized) {
        struct wlr_box geo;
        wlr_xdg_surface_get_geometry(view->xdg_toplevel->base, &geo);
        if (geo.width > 0) {
            view->pending_center = false;
            if (view->saved_geo.width > 0 && view->scene_tree != NULL) {
                /* saved coords are visible coords, geo.x/y the offsets */
                wlr_scene_node_set_position(&view->scene_tree->node,
                    view->saved_geo.x - geo.x, view->saved_geo.y - geo.y);
            } else {
                view_center(view);
            }
        }
    }
    /* keep the frame sized/placed to the (possibly just-resized) window */
    if (view->decorated || view_should_decorate(view)) {
        view_decoration_update(view);
    }
}

static void xdg_toplevel_destroy(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, destroy);
    wl_list_remove(&view->map.link);
    wl_list_remove(&view->unmap.link);
    wl_list_remove(&view->commit.link);
    wl_list_remove(&view->destroy.link);
    wl_list_remove(&view->request_move.link);
    wl_list_remove(&view->request_resize.link);
    wl_list_remove(&view->request_maximize.link);
    wl_list_remove(&view->request_minimize.link);
    wl_list_remove(&view->request_fullscreen.link);
    wl_list_remove(&view->set_title.link);
    wl_list_remove(&view->set_app_id.link);
    view_free(view);
}

static void xdg_toplevel_request_move(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, request_move);
    begin_interactive(view, MJC_CURSOR_MOVE, 0);
}

static void xdg_toplevel_request_resize(struct wl_listener *listener, void *data) {
    struct wlr_xdg_toplevel_resize_event *event = data;
    struct mjc_view *view = wl_container_of(listener, view, request_resize);
    begin_interactive(view, MJC_CURSOR_RESIZE, event->edges);
}

static void xdg_toplevel_request_maximize(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, request_maximize);
    if (view->xdg_toplevel->base->initialized) {
        mjc_view_set_maximized(view, view->xdg_toplevel->requested.maximized);
        wlr_xdg_surface_schedule_configure(view->xdg_toplevel->base);
    }
}

static void xdg_toplevel_request_minimize(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, request_minimize);
    mjc_view_set_minimized(view, true);
}

static void xdg_toplevel_request_fullscreen(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, request_fullscreen);
    if (view->xdg_toplevel->base->initialized) {
        wlr_xdg_surface_schedule_configure(view->xdg_toplevel->base);
    }
}

static void xdg_toplevel_set_title(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, set_title);
    deco_repaint(view);
    if (view->server->cbs.view_title != NULL) {
        view->server->cbs.view_title(view->server->ud, view,
            view->xdg_toplevel->title);
    }
}

static void xdg_toplevel_set_app_id(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, set_app_id);
    if (view->server->cbs.view_app_id != NULL) {
        view->server->cbs.view_app_id(view->server->ud, view,
            view->xdg_toplevel->app_id);
    }
}

static void server_new_xdg_toplevel(struct wl_listener *listener, void *data) {
    struct mjc_server *server =
        wl_container_of(listener, server, new_xdg_toplevel);
    struct wlr_xdg_toplevel *xdg_toplevel = data;

    struct mjc_view *view = view_create(server, false);
    view->xdg_toplevel = xdg_toplevel;
    /* wayland toplevels default to frameless: a client that draws its own CSD (GTK)
     * never negotiates xdg-decoration, and adding a server frame would double it up.
     * The frame is enabled only when the client requests SERVER_SIDE (Qt, libdecor,
     * SDL…) in decoration_request_mode. xwayland/X11 windows have no CSD and are
     * always decorated. */
    view->deco_disabled = true;
    view->scene_tree = wlr_scene_xdg_surface_create(
        server->layers[MJC_LAYER_NORMAL], xdg_toplevel->base);
    view->scene_tree->node.data = view;
    xdg_toplevel->base->data = view->scene_tree;

    view->map.notify = xdg_toplevel_map;
    wl_signal_add(&xdg_toplevel->base->surface->events.map, &view->map);
    view->unmap.notify = xdg_toplevel_unmap;
    wl_signal_add(&xdg_toplevel->base->surface->events.unmap, &view->unmap);
    view->commit.notify = xdg_toplevel_commit;
    wl_signal_add(&xdg_toplevel->base->surface->events.commit, &view->commit);
    view->destroy.notify = xdg_toplevel_destroy;
    wl_signal_add(&xdg_toplevel->events.destroy, &view->destroy);

    view->request_move.notify = xdg_toplevel_request_move;
    wl_signal_add(&xdg_toplevel->events.request_move, &view->request_move);
    view->request_resize.notify = xdg_toplevel_request_resize;
    wl_signal_add(&xdg_toplevel->events.request_resize, &view->request_resize);
    view->request_maximize.notify = xdg_toplevel_request_maximize;
    wl_signal_add(&xdg_toplevel->events.request_maximize, &view->request_maximize);
    view->request_minimize.notify = xdg_toplevel_request_minimize;
    wl_signal_add(&xdg_toplevel->events.request_minimize, &view->request_minimize);
    view->request_fullscreen.notify = xdg_toplevel_request_fullscreen;
    wl_signal_add(&xdg_toplevel->events.request_fullscreen, &view->request_fullscreen);
    view->set_title.notify = xdg_toplevel_set_title;
    wl_signal_add(&xdg_toplevel->events.set_title, &view->set_title);
    view->set_app_id.notify = xdg_toplevel_set_app_id;
    wl_signal_add(&xdg_toplevel->events.set_app_id, &view->set_app_id);

    view_emit_new(view);
}

/* popups */

static void xdg_popup_commit(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_popup *popup = wl_container_of(listener, popup, commit);
    if (popup->xdg_popup->base->initial_commit) {
        wlr_xdg_surface_schedule_configure(popup->xdg_popup->base);
    }
}

static void xdg_popup_destroy(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_popup *popup = wl_container_of(listener, popup, destroy);
    wl_list_remove(&popup->commit.link);
    wl_list_remove(&popup->destroy.link);
    free(popup);
}

static void server_new_xdg_popup(struct wl_listener *listener, void *data) {
    (void) listener;
    struct wlr_xdg_popup *xdg_popup = data;

    struct wlr_xdg_surface *parent =
        wlr_xdg_surface_try_from_wlr_surface(xdg_popup->parent);
    if (parent == NULL || parent->data == NULL) {
        return;
    }
    struct wlr_scene_tree *parent_tree = parent->data;
    xdg_popup->base->data =
        wlr_scene_xdg_surface_create(parent_tree, xdg_popup->base);

    struct mjc_popup *popup = calloc(1, sizeof(*popup));
    popup->xdg_popup = xdg_popup;
    popup->commit.notify = xdg_popup_commit;
    wl_signal_add(&xdg_popup->base->surface->events.commit, &popup->commit);
    popup->destroy.notify = xdg_popup_destroy;
    wl_signal_add(&xdg_popup->events.destroy, &popup->destroy);
}

/* decorations - the compositor draws GNOME-style server-side frames. We grant
 * SERVER_SIDE unless the client explicitly demands client-side decorations, in
 * which case we honor it and leave that window chromeless (no server frame). */

static void decoration_request_mode(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_decoration *deco = wl_container_of(listener, deco, request_mode);
    if (!deco->decoration->toplevel->base->initialized) {
        return;
    }
    bool client_wants_csd = deco->decoration->requested_mode ==
        WLR_XDG_TOPLEVEL_DECORATION_V1_MODE_CLIENT_SIDE;
    wlr_xdg_toplevel_decoration_v1_set_mode(deco->decoration, client_wants_csd
        ? WLR_XDG_TOPLEVEL_DECORATION_V1_MODE_CLIENT_SIDE
        : WLR_XDG_TOPLEVEL_DECORATION_V1_MODE_SERVER_SIDE);
    if (deco->view != NULL) {
        deco->view->deco_disabled = client_wants_csd;
        view_decoration_update(deco->view);
    }
}

static void decoration_destroy(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_decoration *deco = wl_container_of(listener, deco, destroy);
    wl_list_remove(&deco->request_mode.link);
    wl_list_remove(&deco->destroy.link);
    free(deco);
}

static void server_new_toplevel_decoration(struct wl_listener *listener,
        void *data) {
    (void) listener;
    struct wlr_xdg_toplevel_decoration_v1 *decoration = data;
    struct mjc_decoration *deco = calloc(1, sizeof(*deco));
    deco->decoration = decoration;
    /* the view was stashed on the xdg surface in server_new_xdg_toplevel */
    struct wlr_scene_tree *tree = decoration->toplevel->base->data;
    deco->view = tree != NULL ? tree->node.data : NULL;
    deco->request_mode.notify = decoration_request_mode;
    wl_signal_add(&decoration->events.request_mode, &deco->request_mode);
    deco->destroy.notify = decoration_destroy;
    wl_signal_add(&decoration->events.destroy, &deco->destroy);
    decoration_request_mode(&deco->request_mode, NULL);
}

/* ------------------------------------------------------------------ */
/* xwayland views                                                      */
/* ------------------------------------------------------------------ */

static void xwayland_surface_map(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, map);
    struct mjc_server *server = view->server;
    struct wlr_xwayland_surface *xsurface = view->xsurface;

    mjc_layer layer = xsurface->override_redirect
        ? MJC_LAYER_OVERLAY : view->layer;
    view->scene_tree = wlr_scene_tree_create(server->layers[layer]);
    view->scene_tree->node.data = view;
    wlr_scene_surface_create(view->scene_tree, xsurface->surface);
    wlr_scene_node_set_position(&view->scene_tree->node,
        xsurface->x, xsurface->y);

    view->mapped = true;
    /* X clients that did not place themselves land at 0,0 - center those */
    if (!xsurface->override_redirect && xsurface->x == 0 && xsurface->y == 0) {
        view_center(view);
    }
    view_emit_map(view);
    if (!xsurface->override_redirect) {
        mjc_view_focus(view);
    }
    view_decoration_update(view);
}

static void xwayland_surface_unmap(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, unmap);
    view->mapped = false;
    clear_focus_if(view->server, view);
    /* emit while the scene node still exists so the policy side can
     * read the last window geometry */
    view_emit_unmap(view);
    /* tear down the frame before the scene_tree (its children) is destroyed */
    view_decoration_destroy(view);
    if (view->scene_tree != NULL) {
        wlr_scene_node_destroy(&view->scene_tree->node);
        view->scene_tree = NULL;
    }
}

static void xwayland_surface_associate(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, associate);
    view->map.notify = xwayland_surface_map;
    wl_signal_add(&view->xsurface->surface->events.map, &view->map);
    view->unmap.notify = xwayland_surface_unmap;
    wl_signal_add(&view->xsurface->surface->events.unmap, &view->unmap);
}

static void xwayland_surface_dissociate(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, dissociate);
    wl_list_remove(&view->map.link);
    wl_list_remove(&view->unmap.link);
}

static void xwayland_surface_destroy(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, destroy);
    wl_list_remove(&view->associate.link);
    wl_list_remove(&view->dissociate.link);
    wl_list_remove(&view->destroy.link);
    wl_list_remove(&view->request_configure.link);
    wl_list_remove(&view->request_move.link);
    wl_list_remove(&view->request_resize.link);
    wl_list_remove(&view->request_minimize.link);
    wl_list_remove(&view->request_maximize.link);
    wl_list_remove(&view->set_title.link);
    wl_list_remove(&view->set_app_id.link);
    wl_list_remove(&view->set_geometry.link);
    view_decoration_destroy(view);
    if (view->scene_tree != NULL) {
        wlr_scene_node_destroy(&view->scene_tree->node);
        view->scene_tree = NULL;
    }
    view_free(view);
}

static void xwayland_surface_request_configure(struct wl_listener *listener,
        void *data) {
    struct mjc_view *view = wl_container_of(listener, view, request_configure);
    struct wlr_xwayland_surface_configure_event *event = data;
    wlr_xwayland_surface_configure(view->xsurface,
        event->x, event->y, event->width, event->height);
    if (view->scene_tree != NULL) {
        wlr_scene_node_set_position(&view->scene_tree->node, event->x, event->y);
    }
    if (view->decorated) {
        view_decoration_update(view);
    }
}

static void xwayland_surface_set_geometry(struct wl_listener *listener,
        void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, set_geometry);
    if (view->scene_tree != NULL && view->xsurface != NULL) {
        wlr_scene_node_set_position(&view->scene_tree->node,
            view->xsurface->x, view->xsurface->y);
    }
    if (view->decorated) {
        view_decoration_update(view);
    }
}

static void xwayland_surface_request_move(struct wl_listener *listener,
        void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, request_move);
    begin_interactive(view, MJC_CURSOR_MOVE, 0);
}

static void xwayland_surface_request_resize(struct wl_listener *listener,
        void *data) {
    struct mjc_view *view = wl_container_of(listener, view, request_resize);
    struct wlr_xwayland_resize_event *event = data;
    begin_interactive(view, MJC_CURSOR_RESIZE, event->edges);
}

static void xwayland_surface_request_minimize(struct wl_listener *listener,
        void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, request_minimize);
    mjc_view_set_minimized(view, true);
}

static void xwayland_surface_request_maximize(struct wl_listener *listener,
        void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, request_maximize);
    struct wlr_xwayland_surface *xsurface = view->xsurface;
    if (xsurface == NULL) {
        return;
    }
    /* the client announces the state it wants via _NET_WM_STATE */
    mjc_view_set_maximized(view,
        xsurface->maximized_vert && xsurface->maximized_horz);
}

static void xwayland_surface_set_title(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, set_title);
    deco_repaint(view);
    if (view->server->cbs.view_title != NULL) {
        view->server->cbs.view_title(view->server->ud, view,
            view->xsurface->title);
    }
}

static void xwayland_surface_set_class(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_view *view = wl_container_of(listener, view, set_app_id);
    if (view->server->cbs.view_app_id != NULL) {
        view->server->cbs.view_app_id(view->server->ud, view,
            view->xsurface->class);
    }
}

static void server_new_xwayland_surface(struct wl_listener *listener, void *data) {
    struct mjc_server *server =
        wl_container_of(listener, server, new_xwayland_surface);
    struct wlr_xwayland_surface *xsurface = data;

    struct mjc_view *view = view_create(server, true);
    view->xsurface = xsurface;
    if (xsurface->override_redirect) {
        view->focusable = false;
    }
    xsurface->data = view;

    view->associate.notify = xwayland_surface_associate;
    wl_signal_add(&xsurface->events.associate, &view->associate);
    view->dissociate.notify = xwayland_surface_dissociate;
    wl_signal_add(&xsurface->events.dissociate, &view->dissociate);
    view->destroy.notify = xwayland_surface_destroy;
    wl_signal_add(&xsurface->events.destroy, &view->destroy);
    view->request_configure.notify = xwayland_surface_request_configure;
    wl_signal_add(&xsurface->events.request_configure, &view->request_configure);
    view->request_move.notify = xwayland_surface_request_move;
    wl_signal_add(&xsurface->events.request_move, &view->request_move);
    view->request_resize.notify = xwayland_surface_request_resize;
    wl_signal_add(&xsurface->events.request_resize, &view->request_resize);
    view->request_minimize.notify = xwayland_surface_request_minimize;
    wl_signal_add(&xsurface->events.request_minimize, &view->request_minimize);
    view->request_maximize.notify = xwayland_surface_request_maximize;
    wl_signal_add(&xsurface->events.request_maximize, &view->request_maximize);
    view->set_title.notify = xwayland_surface_set_title;
    wl_signal_add(&xsurface->events.set_title, &view->set_title);
    view->set_app_id.notify = xwayland_surface_set_class;
    wl_signal_add(&xsurface->events.set_class, &view->set_app_id);
    view->set_geometry.notify = xwayland_surface_set_geometry;
    wl_signal_add(&xsurface->events.set_geometry, &view->set_geometry);

    view_emit_new(view);
}

static void server_xwayland_ready(struct wl_listener *listener, void *data) {
    (void) data;
    struct mjc_server *server = wl_container_of(listener, server, xwayland_ready);
    wlr_xwayland_set_seat(server->xwayland, server->seat);
}

/* ------------------------------------------------------------------ */
/* sigchld                                                             */
/* ------------------------------------------------------------------ */

static int handle_sigchld(int signal_number, void *data) {
    (void) signal_number;
    struct mjc_server *server = data;
    for (int i = 0; i < server->spawned_count;) {
        int status;
        pid_t pid = waitpid(server->spawned_pids[i], &status, WNOHANG);
        if (pid > 0) {
            server->spawned_pids[i] =
                server->spawned_pids[--server->spawned_count];
            if (server->cbs.child_exit != NULL) {
                server->cbs.child_exit(server->ud, (int) pid, status);
            }
        } else {
            i++;
        }
    }
    return 0;
}

/* True when the GLES2 renderer ended up on software rendering (llvmpipe/swrast) — i.e. there is
 * no usable hardware GL. In that case the Compose shell (Skiko) cannot create an OpenGL/GLX
 * context on the (also software) XWayland and must use its CPU raster backend, so the compositor
 * exports SKIKO_RENDER_API=SOFTWARE for the shell. On a real GPU it returns false and the shell
 * uses hardware GL. Queries GL_RENDERER via the renderer's own EGL context (surfaceless). */
static bool mjc_renderer_is_software(struct wlr_renderer *renderer) {
    if (!wlr_renderer_is_gles2(renderer)) {
        return false;
    }
    struct wlr_egl *egl = wlr_gles2_renderer_get_egl(renderer);
    if (egl == NULL) {
        return false;
    }
    EGLDisplay dpy = wlr_egl_get_display(egl);
    EGLContext ctx = wlr_egl_get_context(egl);
    if (dpy == EGL_NO_DISPLAY || ctx == EGL_NO_CONTEXT) {
        return false;
    }
    if (!eglMakeCurrent(dpy, EGL_NO_SURFACE, EGL_NO_SURFACE, ctx)) {
        return false;
    }
    const char *gl_renderer = (const char *) glGetString(GL_RENDERER);
    bool software = gl_renderer != NULL && (
        strstr(gl_renderer, "llvmpipe") != NULL ||
        strstr(gl_renderer, "softpipe") != NULL ||
        strstr(gl_renderer, "swrast") != NULL ||
        strstr(gl_renderer, "SWR") != NULL ||
        strstr(gl_renderer, "Software") != NULL);
    eglMakeCurrent(dpy, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    return software;
}

/* ------------------------------------------------------------------ */
/* public API                                                          */
/* ------------------------------------------------------------------ */

mjc_server *mjc_create(void) {
    struct mjc_server *server = calloc(1, sizeof(*server));
    wl_list_init(&server->views);
    wl_list_init(&server->keyboards);
    wl_list_init(&server->fd_sources);
    return server;
}

bool mjc_start(mjc_server *server, const mjc_callbacks *callbacks, void *userdata) {
    server->cbs = *callbacks;
    server->ud = userdata;

    /* default decoration theme (dark slate + near-white); the shell overrides this
     * from its live background palette via mjc_set_decoration_theme over IPC */
    server->deco_bg[0] = 0.16f;
    server->deco_bg[1] = 0.17f;
    server->deco_bg[2] = 0.20f;
    server->deco_fg[0] = 0.92f;
    server->deco_fg[1] = 0.93f;
    server->deco_fg[2] = 0.95f;

    wlr_log_init(WLR_INFO, NULL);

    /* nested-output size, 16:9 default; override with MJDEVC_OUTPUT=WIDTHxHEIGHT */
    server->output_w = 1280;
    server->output_h = 720;
    const char *out_env = getenv("MJDEVC_OUTPUT");
    if (out_env != NULL) {
        int w = 0, h = 0;
        if (sscanf(out_env, "%dx%d", &w, &h) == 2 && w > 0 && h > 0) {
            server->output_w = w;
            server->output_h = h;
        }
    }

    server->display = wl_display_create();
    server->loop = wl_display_get_event_loop(server->display);
    server->backend = wlr_backend_autocreate(server->loop, &server->session);
    if (server->backend == NULL) {
        fprintf(stderr, "mjdevc: failed to create wlr_backend\n");
        return false;
    }

    server->renderer = wlr_renderer_autocreate(server->backend);
    if (server->renderer == NULL) {
        fprintf(stderr, "mjdevc: failed to create wlr_renderer\n");
        return false;
    }
    wlr_renderer_init_wl_display(server->renderer, server->display);

    server->allocator = wlr_allocator_autocreate(server->backend, server->renderer);
    if (server->allocator == NULL) {
        fprintf(stderr, "mjdevc: failed to create wlr_allocator\n");
        return false;
    }

    server->compositor = wlr_compositor_create(server->display, 5, server->renderer);
    wlr_subcompositor_create(server->display);
    wlr_data_device_manager_create(server->display);

    server->output_layout = wlr_output_layout_create(server->display);
    server->new_output.notify = server_new_output;
    wl_signal_add(&server->backend->events.new_output, &server->new_output);

    server->scene = wlr_scene_create();
    server->scene_layout =
        wlr_scene_attach_output_layout(server->scene, server->output_layout);
    for (int i = 0; i < 5; i++) {
        server->layers[i] = wlr_scene_tree_create(&server->scene->tree);
    }

    /* injection feedback marker: a 16x16 white square in the overlay layer, hidden until
     * input is injected, then shown at the pointer and faded out (Android show-touches style) */
    const float marker_hidden[4] = {0.0f, 0.0f, 0.0f, 0.0f};
    server->inject_marker = wlr_scene_rect_create(
        server->layers[MJC_LAYER_OVERLAY], 16, 16, marker_hidden);
    wlr_scene_node_set_enabled(&server->inject_marker->node, false);

    server->xdg_shell = wlr_xdg_shell_create(server->display, 3);
    server->new_xdg_toplevel.notify = server_new_xdg_toplevel;
    wl_signal_add(&server->xdg_shell->events.new_toplevel, &server->new_xdg_toplevel);
    server->new_xdg_popup.notify = server_new_xdg_popup;
    wl_signal_add(&server->xdg_shell->events.new_popup, &server->new_xdg_popup);

    server->xdg_decoration_mgr = wlr_xdg_decoration_manager_v1_create(server->display);
    server->new_toplevel_decoration.notify = server_new_toplevel_decoration;
    wl_signal_add(&server->xdg_decoration_mgr->events.new_toplevel_decoration,
        &server->new_toplevel_decoration);

    server->cursor = wlr_cursor_create();
    wlr_cursor_attach_output_layout(server->cursor, server->output_layout);
    server->cursor_mgr = wlr_xcursor_manager_create(NULL, 24);
    server->cursor_mode = MJC_CURSOR_PASSTHROUGH;
    server->cursor_motion.notify = server_cursor_motion;
    wl_signal_add(&server->cursor->events.motion, &server->cursor_motion);
    server->cursor_motion_absolute.notify = server_cursor_motion_absolute;
    wl_signal_add(&server->cursor->events.motion_absolute,
        &server->cursor_motion_absolute);
    server->cursor_button.notify = server_cursor_button;
    wl_signal_add(&server->cursor->events.button, &server->cursor_button);
    server->cursor_axis.notify = server_cursor_axis;
    wl_signal_add(&server->cursor->events.axis, &server->cursor_axis);
    server->cursor_frame.notify = server_cursor_frame;
    wl_signal_add(&server->cursor->events.frame, &server->cursor_frame);

    server->new_input.notify = server_new_input;
    wl_signal_add(&server->backend->events.new_input, &server->new_input);
    server->seat = wlr_seat_create(server->display, "seat0");
    server->request_cursor.notify = seat_request_cursor;
    wl_signal_add(&server->seat->events.request_set_cursor, &server->request_cursor);
    server->request_set_selection.notify = seat_request_set_selection;
    wl_signal_add(&server->seat->events.request_set_selection,
        &server->request_set_selection);

    const char *socket = wl_display_add_socket_auto(server->display);
    if (socket == NULL) {
        fprintf(stderr, "mjdevc: failed to create wayland socket\n");
        return false;
    }
    server->socket = socket;

    server->xwayland = wlr_xwayland_create(server->display, server->compositor, true);
    if (server->xwayland != NULL) {
        server->new_xwayland_surface.notify = server_new_xwayland_surface;
        wl_signal_add(&server->xwayland->events.new_surface,
            &server->new_xwayland_surface);
        server->xwayland_ready.notify = server_xwayland_ready;
        wl_signal_add(&server->xwayland->events.ready, &server->xwayland_ready);
    }

    server->sigchld_source = wl_event_loop_add_signal(server->loop,
        SIGCHLD, handle_sigchld, server);

    if (!wlr_backend_start(server->backend)) {
        fprintf(stderr, "mjdevc: failed to start backend\n");
        return false;
    }

    setenv("WAYLAND_DISPLAY", socket, true);
    if (server->xwayland != NULL) {
        setenv("DISPLAY", server->xwayland->display_name, true);
    }
    /* GPU vs software decision for the Compose shell (Skiko), exported into its environment
     * before it is spawned. Software GL (VM / no GPU) -> Skiko CPU raster (else it crashes with
     * "Cannot create Linux GL context"); real GPU -> leave unset so Skiko uses hardware GL. */
    if (mjc_renderer_is_software(server->renderer)) {
        setenv("SKIKO_RENDER_API", "SOFTWARE", true);
        fprintf(stderr, "mjdevc: software GL renderer -> SKIKO_RENDER_API=SOFTWARE for the shell\n");
    } else {
        unsetenv("SKIKO_RENDER_API");
        fprintf(stderr, "mjdevc: hardware GL renderer -> shell uses Skiko OpenGL\n");
    }

    if (server->cbs.ready != NULL) {
        server->cbs.ready(server->ud);
    }
    return true;
}

void mjc_run(mjc_server *server) {
    wl_display_run(server->display);
}

void mjc_terminate(mjc_server *server) {
    wl_display_terminate(server->display);
}

void mjc_destroy(mjc_server *server) {
    if (server->xwayland != NULL) {
        wlr_xwayland_destroy(server->xwayland);
    }
    wl_display_destroy_clients(server->display);
    wlr_scene_node_destroy(&server->scene->tree.node);
    wlr_xcursor_manager_destroy(server->cursor_mgr);
    wlr_cursor_destroy(server->cursor);
    wlr_allocator_destroy(server->allocator);
    wlr_renderer_destroy(server->renderer);
    wlr_backend_destroy(server->backend);
    wl_display_destroy(server->display);
    free(server);
}

const char *mjc_socket_name(mjc_server *server) {
    return server->socket;
}

const char *mjc_xwayland_display(mjc_server *server) {
    return server->xwayland != NULL ? server->xwayland->display_name : NULL;
}

void mjc_output_size(mjc_server *server, int *width, int *height) {
    struct wlr_box box;
    wlr_output_layout_get_box(server->output_layout, NULL, &box);
    *width = box.width;
    *height = box.height;
}

int mjc_spawn(mjc_server *server, const char *command) {
    pid_t pid = fork();
    if (pid < 0) {
        return -1;
    }
    if (pid == 0) {
        setsid();
        sigset_t set;
        sigemptyset(&set);
        sigprocmask(SIG_SETMASK, &set, NULL);
        execl("/bin/sh", "/bin/sh", "-c", command, (void *) NULL);
        _exit(127);
    }
    if (server->spawned_count <
            (int) (sizeof(server->spawned_pids) / sizeof(server->spawned_pids[0]))) {
        server->spawned_pids[server->spawned_count++] = pid;
    }
    return (int) pid;
}

/* unix socket helpers */

#include <fcntl.h>
#include <sys/socket.h>
#include <sys/un.h>

int mjc_unix_listen(const char *path, int backlog) {
    int fd = socket(AF_UNIX, SOCK_STREAM | SOCK_NONBLOCK | SOCK_CLOEXEC, 0);
    if (fd < 0) {
        return -1;
    }
    struct sockaddr_un addr = {0};
    addr.sun_family = AF_UNIX;
    if (strlen(path) >= sizeof(addr.sun_path)) {
        close(fd);
        return -1;
    }
    strncpy(addr.sun_path, path, sizeof(addr.sun_path) - 1);
    unlink(path);
    if (bind(fd, (struct sockaddr *) &addr, sizeof(addr)) != 0 ||
            listen(fd, backlog) != 0) {
        close(fd);
        return -1;
    }
    return fd;
}

int mjc_unix_accept(int server_fd) {
    int fd = accept(server_fd, NULL, NULL);
    if (fd >= 0) {
        fcntl(fd, F_SETFL, O_NONBLOCK);
    }
    return fd;
}

/* fd integration */

static int handle_fd_event(int fd, uint32_t mask, void *data) {
    struct mjc_fd_source *source = data;
    if (source->server->cbs.fd_event != NULL) {
        return source->server->cbs.fd_event(source->server->ud, fd, mask);
    }
    return 0;
}

bool mjc_loop_add_fd(mjc_server *server, int fd, uint32_t mask) {
    struct mjc_fd_source *source = calloc(1, sizeof(*source));
    source->server = server;
    source->fd = fd;
    source->source = wl_event_loop_add_fd(server->loop, fd, mask,
        handle_fd_event, source);
    if (source->source == NULL) {
        free(source);
        return false;
    }
    wl_list_insert(&server->fd_sources, &source->link);
    return true;
}

void mjc_loop_remove_fd(mjc_server *server, int fd) {
    struct mjc_fd_source *source, *tmp;
    wl_list_for_each_safe(source, tmp, &server->fd_sources, link) {
        if (source->fd == fd) {
            wl_event_source_remove(source->source);
            wl_list_remove(&source->link);
            free(source);
            return;
        }
    }
}

/* views */

uint64_t mjc_view_id(mjc_view *view) {
    return view->id;
}

int mjc_view_pid(mjc_view *view) {
    if (view->is_xwayland) {
        return view->xsurface != NULL ? (int) view->xsurface->pid : 0;
    }
    pid_t pid = 0;
    struct wlr_surface *surface = view_surface(view);
    if (surface != NULL) {
        struct wl_client *client = wl_resource_get_client(surface->resource);
        wl_client_get_credentials(client, &pid, NULL, NULL);
    }
    return (int) pid;
}

const char *mjc_view_title(mjc_view *view) {
    if (view->is_xwayland) {
        return view->xsurface != NULL ? view->xsurface->title : NULL;
    }
    return view->xdg_toplevel->title;
}

const char *mjc_view_app_id(mjc_view *view) {
    if (view->is_xwayland) {
        return view->xsurface != NULL ? view->xsurface->class : NULL;
    }
    return view->xdg_toplevel->app_id;
}

bool mjc_view_is_xwayland(mjc_view *view) {
    return view->is_xwayland;
}

bool mjc_view_is_mapped(mjc_view *view) {
    return view->mapped;
}

bool mjc_view_is_minimized(mjc_view *view) {
    return view->minimized;
}

bool mjc_view_is_maximized(mjc_view *view) {
    return view->maximized;
}

bool mjc_view_is_focused(mjc_view *view) {
    return view->server->focused_view == view;
}

void mjc_view_get_geometry(mjc_view *view, int *x, int *y, int *width, int *height) {
    struct wlr_box box = {0};
    if (view->scene_tree != NULL || !view->is_xwayland) {
        view_current_geometry(view, &box);
    }
    *x = box.x;
    *y = box.y;
    *width = box.width;
    *height = box.height;
}

void mjc_view_close(mjc_view *view) {
    if (view->is_xwayland) {
        if (view->xsurface != NULL) {
            wlr_xwayland_surface_close(view->xsurface);
        }
    } else {
        wlr_xdg_toplevel_send_close(view->xdg_toplevel);
    }
}

void mjc_view_set_minimized(mjc_view *view, bool minimized) {
    if (view->minimized == minimized) {
        return;
    }
    view->minimized = minimized;
    if (view->scene_tree != NULL) {
        wlr_scene_node_set_enabled(&view->scene_tree->node, !minimized);
    }
    if (view->is_xwayland && view->xsurface != NULL) {
        wlr_xwayland_surface_set_minimized(view->xsurface, minimized);
    }
    if (minimized) {
        clear_focus_if(view->server, view);
    } else {
        mjc_view_focus(view);
    }
}

void mjc_view_set_maximized(mjc_view *view, bool maximized) {
    struct mjc_server *server = view->server;
    if (view->maximized == maximized) {
        return;
    }
    struct wlr_box box;
    wlr_output_layout_get_box(server->output_layout, NULL, &box);
    if (maximized && view->mapped && view->scene_tree != NULL) {
        /* remember the floating geometry (visible coords) for restore */
        view_current_geometry(view, &view->saved_geo);
    }
    view->maximized = maximized;
    if (view->is_xwayland) {
        if (view->xsurface == NULL) {
            return;
        }
        wlr_xwayland_surface_set_maximized(view->xsurface, maximized);
        if (maximized) {
            wlr_xwayland_surface_configure(view->xsurface,
                (int16_t) box.x, (int16_t) box.y,
                (uint16_t) box.width, (uint16_t) box.height);
            if (view->scene_tree != NULL) {
                wlr_scene_node_set_position(&view->scene_tree->node, box.x, box.y);
            }
        } else if (view->saved_geo.width > 0) {
            wlr_xwayland_surface_configure(view->xsurface,
                (int16_t) view->saved_geo.x, (int16_t) view->saved_geo.y,
                (uint16_t) view->saved_geo.width, (uint16_t) view->saved_geo.height);
            if (view->scene_tree != NULL) {
                wlr_scene_node_set_position(&view->scene_tree->node,
                    view->saved_geo.x, view->saved_geo.y);
            }
        }
    } else {
        wlr_xdg_toplevel_set_maximized(view->xdg_toplevel, maximized);
        if (maximized) {
            wlr_xdg_toplevel_set_size(view->xdg_toplevel, box.width, box.height);
            if (view->scene_tree != NULL) {
                wlr_scene_node_set_position(&view->scene_tree->node, box.x, box.y);
            }
        } else {
            /* 0x0 when no remembered size - the client picks its own;
             * placement happens on the commit that carries the new size,
             * when the decoration offsets are known again */
            wlr_xdg_toplevel_set_size(view->xdg_toplevel,
                view->saved_geo.width, view->saved_geo.height);
            view->pending_center = true;
        }
    }
    /* xwayland sizes itself synchronously here (no commit round-trip), so refresh
     * the frame now; xdg follows up on its next commit */
    if (view->decorated) {
        view_decoration_update(view);
    }
}

void mjc_view_set_layer(mjc_view *view, mjc_layer layer) {
    view->layer = layer;
    if (view->scene_tree != NULL) {
        wlr_scene_node_reparent(&view->scene_tree->node,
            view->server->layers[layer]);
    }
    /* only normal-layer app windows get a server-side frame; shell surfaces
     * (wallpaper, panels, menus) live in other layers and stay undecorated */
    view_decoration_update(view);
}

void mjc_view_set_focusable(mjc_view *view, bool focusable) {
    view->focusable = focusable;
    if (!focusable) {
        clear_focus_if(view->server, view);
    }
}

void mjc_view_set_chrome(mjc_view *view, bool chrome) {
    view->is_chrome = chrome;
    view_decoration_update(view);
}

void mjc_view_set_decorated(mjc_view *view, bool decorated) {
    view->deco_disabled = !decorated;
    view_decoration_update(view);
}

void mjc_set_decoration_theme(mjc_server *server,
        float bg_r, float bg_g, float bg_b,
        float fg_r, float fg_g, float fg_b) {
    server->deco_bg[0] = bg_r;
    server->deco_bg[1] = bg_g;
    server->deco_bg[2] = bg_b;
    server->deco_fg[0] = fg_r;
    server->deco_fg[1] = fg_g;
    server->deco_fg[2] = fg_b;
    /* re-skin every currently decorated window with the new palette */
    struct mjc_view *view;
    wl_list_for_each(view, &server->views, link) {
        if (view->decorated) {
            view->deco_w = 0; /* force a re-render at the same width */
            view_decoration_update(view);
            /* refresh the border tint too */
            const float border_col[4] = {
                bg_r * 0.6f, bg_g * 0.6f, bg_b * 0.6f, 1.0f,
            };
            for (int i = 0; i < 3; i++) {
                if (view->deco_border[i] != NULL) {
                    wlr_scene_rect_set_color(view->deco_border[i], border_col);
                }
            }
        }
    }
}

void mjc_view_set_position(mjc_view *view, int x, int y) {
    if (view->scene_tree != NULL) {
        wlr_scene_node_set_position(&view->scene_tree->node, x, y);
    }
    if (view->is_xwayland && view->xsurface != NULL) {
        wlr_xwayland_surface_configure(view->xsurface,
            (int16_t) x, (int16_t) y,
            (uint16_t) view->xsurface->width, (uint16_t) view->xsurface->height);
    }
}

void mjc_view_set_geometry(mjc_view *view, int x, int y, int width, int height) {
    if (width <= 0 || height <= 0) {
        return;
    }
    if (view->maximized) {
        /* keep it as the restore target for the next unmaximize */
        view->saved_geo.x = x;
        view->saved_geo.y = y;
        view->saved_geo.width = width;
        view->saved_geo.height = height;
        return;
    }
    if (view->is_xwayland) {
        if (view->xsurface != NULL) {
            wlr_xwayland_surface_configure(view->xsurface,
                (int16_t) x, (int16_t) y, (uint16_t) width, (uint16_t) height);
        }
        if (view->scene_tree != NULL) {
            wlr_scene_node_set_position(&view->scene_tree->node, x, y);
        }
    } else {
        wlr_xdg_toplevel_set_size(view->xdg_toplevel, width, height);
        if (view->scene_tree != NULL) {
            struct wlr_box off;
            wlr_xdg_surface_get_geometry(view->xdg_toplevel->base, &off);
            wlr_scene_node_set_position(&view->scene_tree->node,
                x - off.x, y - off.y);
        }
    }
    if (view->decorated) {
        view_decoration_update(view);
    }
}

/* ------------------------------------------------------------------ */
/* input injection (ipc-driven, for automated/headless testing)        */
/* ------------------------------------------------------------------ */

static uint32_t mjc_now_msec(void) {
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return (uint32_t) (now.tv_sec * 1000 + now.tv_nsec / 1000000);
}

#define MJC_INJECT_MARKER_SIZE 16
#define MJC_INJECT_FADE_STEP 0.08f
#define MJC_INJECT_FADE_INTERVAL_MS 40

/* step the injection marker's alpha down; disables it once fully faded */
static int inject_marker_fade(void *data) {
    struct mjc_server *server = data;
    if (server->inject_marker == NULL) {
        return 0;
    }
    server->inject_alpha -= MJC_INJECT_FADE_STEP;
    if (server->inject_alpha <= 0.0f) {
        server->inject_alpha = 0.0f;
        wlr_scene_node_set_enabled(&server->inject_marker->node, false);
        return 0;
    }
    float a = server->inject_alpha;
    const float color[4] = {a, a, a, a}; /* premultiplied white */
    wlr_scene_rect_set_color(server->inject_marker, color);
    if (server->inject_fade_timer != NULL) {
        wl_event_source_timer_update(server->inject_fade_timer,
            MJC_INJECT_FADE_INTERVAL_MS);
    }
    return 0;
}

/* show the injection marker at the current cursor position at full opacity and (re)arm
 * the fade timer; called on every injected pointer event so it stays bright while moving */
static void inject_marker_show(struct mjc_server *server) {
    if (server->inject_marker == NULL) {
        return;
    }
    wlr_scene_node_set_position(&server->inject_marker->node,
        (int) server->cursor->x - MJC_INJECT_MARKER_SIZE / 2,
        (int) server->cursor->y - MJC_INJECT_MARKER_SIZE / 2);
    server->inject_alpha = 1.0f;
    const float color[4] = {1.0f, 1.0f, 1.0f, 1.0f};
    wlr_scene_rect_set_color(server->inject_marker, color);
    wlr_scene_node_raise_to_top(&server->inject_marker->node);
    wlr_scene_node_set_enabled(&server->inject_marker->node, true);
    /* keep the marker persistently visible for now (behaves like a mouse cursor); the
     * inactivity fadeout is disabled until we want it back */
}

void mjc_pointer_move(mjc_server *server, int x, int y) {
    if (server == NULL) {
        return;
    }
    /* warp to the closest valid layout point, then run the normal motion path so
     * pointer focus, focus-follows-mouse and the ipc pointer broadcast all fire */
    wlr_cursor_warp_closest(server->cursor, NULL, (double) x, (double) y);
    process_cursor_motion(server, mjc_now_msec());
    inject_marker_show(server);
}

void mjc_pointer_button(mjc_server *server, uint32_t button, bool pressed) {
    if (server == NULL) {
        return;
    }
    enum wl_pointer_button_state state = pressed
        ? WL_POINTER_BUTTON_STATE_PRESSED
        : WL_POINTER_BUTTON_STATE_RELEASED;
    wlr_seat_pointer_notify_button(server->seat, mjc_now_msec(), button, state);
    if (!pressed) {
        server->cursor_mode = MJC_CURSOR_PASSTHROUGH;
        server->grabbed_view = NULL;
    } else {
        double sx, sy;
        struct wlr_surface *surface = NULL;
        struct mjc_view *view = desktop_view_at(server,
            server->cursor->x, server->cursor->y, &surface, &sx, &sy);
        if (view != NULL && view->focusable) {
            mjc_view_focus(view);
        }
    }
    wlr_seat_pointer_notify_frame(server->seat);
    inject_marker_show(server);
}

void mjc_key(mjc_server *server, uint32_t keycode, bool pressed) {
    if (server == NULL) {
        return;
    }
    struct wlr_keyboard *keyboard = wlr_seat_get_keyboard(server->seat);
    if (keyboard == NULL) {
        if (wl_list_empty(&server->keyboards)) {
            return;
        }
        struct mjc_keyboard *kb =
            wl_container_of(server->keyboards.next, kb, link);
        keyboard = kb->wlr_keyboard;
    }
    enum wl_keyboard_key_state state = pressed
        ? WL_KEYBOARD_KEY_STATE_PRESSED
        : WL_KEYBOARD_KEY_STATE_RELEASED;
    /* mirror keyboard_handle_key: offer the keysym to the shell global-key hook first
     * (Super/Escape toggles), and only forward to the focused client when unhandled */
    uint32_t xkb_keycode = keycode + 8;
    const xkb_keysym_t *syms;
    int nsyms = xkb_state_key_get_syms(keyboard->xkb_state, xkb_keycode, &syms);
    uint32_t modifiers = wlr_keyboard_get_modifiers(keyboard);
    bool handled = false;
    if (pressed) {
        for (int i = 0; i < nsyms; i++) {
            if (server->cbs.key != NULL &&
                    server->cbs.key(server->ud, syms[i], modifiers, true)) {
                handled = true;
            }
        }
    }
    if (!handled) {
        wlr_seat_set_keyboard(server->seat, keyboard);
        wlr_seat_keyboard_notify_key(server->seat, mjc_now_msec(), keycode, state);
    }
}
