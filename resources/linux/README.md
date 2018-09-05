The contents of this directory can be used to install the HTTP File Upload as a
Linux _systemd_ service. This ensures that the proxy is automatically restarted on
system reboot.

To see if your system supports systemd, check for the existence of the directory
`/lib/systemd`

To install the service:

- Modify the fileupload.service file according to the TODO comments inside.
- Copy the file to `/etc/systemd/system`
- Enable the service to start at boot time by running

        sudo systemctl enable fileupload.service

To uninstall the service

1. Run

        sudo systemctl stop fileupload.service
        sudo systemctl disable fileupload.service

2. remove the file you copied to `/etc/systemd/system/fileupload.service`

To start the service manually use

    sudo systemctl start fileupload.service

_This procedure was tested on Ubuntu 17.04_

