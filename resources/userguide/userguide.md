---
title: "Teamscale HTTP File Upload User Guide"
author: "CQSE GmbH"
titlepage: true
titlepage-color: "fd7b34"
titlepage-rule-color: "ffffff"
titlepage-text-color: "ffffff"
colorlinks: true
mainfont: "Futura Std"
monofont: "Bitstream Vera Sans Mono"
mathspec: true
---

# Teamscale HTTP File Upload

Accepts HTTP connections on a port. If a multi-part form data request
is sent to that port with a `file` parameter containing a file, the file
is stored locally.

Run with `--help` to get a list of all available command line arguments.

To change the logging behaviour, configure log4j2 by providing an appropriate
XML configuration via the JVM parameter `-Dlog4j.configurationFile=XMLFILE`,
where `XMLFILE` is the full path to the XML configuration for log4j2. You can
find example configurations in the distribution zip file.

## As a service

See the `windows` and `linux` directories for instructions on how to
install this as a service.
