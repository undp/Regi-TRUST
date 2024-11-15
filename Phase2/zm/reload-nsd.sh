#! /bin/sh

/usr/sbin/nsd-control reload    # prints 'ok'
/usr/sbin/nsd-control reconfig  # prints 'reconfig start, read /etc/nsd/nsd.conf' and 'ok'

