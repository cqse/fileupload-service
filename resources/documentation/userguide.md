# Teamscale HTTP File Upload

Accepts HTTP connections on a port. If a multi-part form data request
is sent to that port with a `file` parameter containing a file, the file
is stored locally. Optionally, a command can be run for every uploaded file.

This program can be used to e.g. bridge data traffic between isolated networks
and facilitate an upload to Teamscale by running an uploader script as the command.

To change the logging behaviour, configure log4j2 by providing an appropriate
XML configuration via the JVM parameter `-Dlog4j.configurationFile=XMLFILE`,
where `XMLFILE` is the full path to the XML configuration for log4j2. You can
find example configurations in this zip file.

## Command line arguments

Run with `--help` to get a list of all available command line arguments.

- `--out`: The directory to which to write the uploaded files.
- `--port`: The port on which to listen ofr connections.
- `--command`: Optional command to run after each upload. `{F}` will be replaced with the path to the uploaded file.

## As a service

See the `windows` and `linux` directories for instructions on how to
install this as a service.

