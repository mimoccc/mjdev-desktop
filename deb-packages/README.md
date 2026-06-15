# extra debs for the iso

Drop any additional `*.deb` here. `make-iso.sh` installs every `*.deb` found in
this directory into the image (after the main desktop deb, no recommends).
Leave empty for a pure wayland + mjdev-desktop image.
