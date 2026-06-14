/*
 * Copyright (c) Milan Jurkulák 2026.
 *  Contact:
 *  e: mimoccc@gmail.com
 *  e: mj@mjdev.org
 *  w: https://mjdev.org
 *
 * Flat C API over wlroots for the mjdev compositor.
 * This header is self contained (no wlroots includes) so it can be
 * processed by Kotlin/Native cinterop directly.
 */

#ifndef MJDEV_COMPOSITOR_SHIM_H
#define MJDEV_COMPOSITOR_SHIM_H

#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct mjc_server mjc_server;
typedef struct mjc_view mjc_view;

typedef enum mjc_layer {
    MJC_LAYER_BACKGROUND = 0,
    MJC_LAYER_BOTTOM = 1,
    MJC_LAYER_NORMAL = 2,
    MJC_LAYER_TOP = 3,
    MJC_LAYER_OVERLAY = 4,
} mjc_layer;

/* mask values for fd events, mirror wayland WL_EVENT_* */
#define MJC_EVENT_READABLE 0x01
#define MJC_EVENT_WRITABLE 0x02
#define MJC_EVENT_HANGUP   0x04
#define MJC_EVENT_ERROR    0x08

/* keyboard modifiers, mirror wlroots WLR_MODIFIER_* */
#define MJC_MOD_SHIFT 0x01
#define MJC_MOD_CAPS  0x02
#define MJC_MOD_CTRL  0x04
#define MJC_MOD_ALT   0x08
#define MJC_MOD_MOD2  0x10
#define MJC_MOD_MOD3  0x20
#define MJC_MOD_LOGO  0x40
#define MJC_MOD_MOD5  0x80

typedef struct mjc_callbacks {
    /* view lifecycle */
    void (*view_new)(void *ud, mjc_view *view, bool xwayland);
    void (*view_map)(void *ud, mjc_view *view);
    void (*view_unmap)(void *ud, mjc_view *view);
    void (*view_destroy)(void *ud, mjc_view *view);
    void (*view_title)(void *ud, mjc_view *view, const char *title);
    void (*view_app_id)(void *ud, mjc_view *view, const char *app_id);
    /* focus changed; view may be NULL when nothing is focused */
    void (*focus_change)(void *ud, mjc_view *view);
    /* keyboard binding hook, return true when the combo was consumed */
    bool (*key)(void *ud, uint32_t keysym, uint32_t modifiers, bool pressed);
    /* fd registered via mjc_loop_add_fd became ready */
    int (*fd_event)(void *ud, int fd, uint32_t mask);
    /* a child process spawned via mjc_spawn exited */
    void (*child_exit)(void *ud, int pid, int status);
    /* backend started, sockets exported - safe to spawn clients */
    void (*ready)(void *ud);
    /* pointer moved, in compositor layout coordinates (throttled) */
    void (*pointer)(void *ud, int x, int y);
} mjc_callbacks;

/* lifecycle */
mjc_server *mjc_create(void);
bool mjc_start(mjc_server *server, const mjc_callbacks *callbacks, void *userdata);
void mjc_run(mjc_server *server);
void mjc_terminate(mjc_server *server);
void mjc_destroy(mjc_server *server);

/* environment */
const char *mjc_socket_name(mjc_server *server);      /* WAYLAND_DISPLAY */
const char *mjc_xwayland_display(mjc_server *server); /* DISPLAY or NULL */
void mjc_output_size(mjc_server *server, int *width, int *height);

/* process spawning (inherits WAYLAND_DISPLAY/DISPLAY), returns pid or -1 */
int mjc_spawn(mjc_server *server, const char *command);

/* event loop integration for the IPC server */
bool mjc_loop_add_fd(mjc_server *server, int fd, uint32_t mask);
void mjc_loop_remove_fd(mjc_server *server, int fd);

/* unix socket helpers (sockaddr_un is unavailable in kotlin/native posix) */
int mjc_unix_listen(const char *path, int backlog); /* nonblocking fd or -1 */
int mjc_unix_accept(int server_fd);                 /* nonblocking fd or -1 */

/* views */
uint64_t mjc_view_id(mjc_view *view);
int mjc_view_pid(mjc_view *view);
const char *mjc_view_title(mjc_view *view);
const char *mjc_view_app_id(mjc_view *view);
bool mjc_view_is_xwayland(mjc_view *view);
bool mjc_view_is_mapped(mjc_view *view);
bool mjc_view_is_minimized(mjc_view *view);
bool mjc_view_is_maximized(mjc_view *view);
bool mjc_view_is_focused(mjc_view *view);
void mjc_view_get_geometry(mjc_view *view, int *x, int *y, int *width, int *height);

void mjc_view_focus(mjc_view *view);
void mjc_view_close(mjc_view *view);
void mjc_view_set_minimized(mjc_view *view, bool minimized);
void mjc_view_set_maximized(mjc_view *view, bool maximized);
void mjc_view_set_layer(mjc_view *view, mjc_layer layer);
void mjc_view_set_focusable(mjc_view *view, bool focusable);
void mjc_view_set_position(mjc_view *view, int x, int y);
void mjc_view_set_geometry(mjc_view *view, int x, int y, int width, int height);

#ifdef __cplusplus
}
#endif

#endif /* MJDEV_COMPOSITOR_SHIM_H */
