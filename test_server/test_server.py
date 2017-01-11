print "Version test"

import sys, os
import socket
import errno
import threading
import multiprocessing
import signal

import random

from ptimer import PeriodicTimer


SOCK = socket.socket(socket.AF_INET, socket.SOCK_STREAM, socket.getprotobyname('TCP'))
SOCK.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
SOCK.bind(('0.0.0.0', 4669))
SOCK.listen(1)

conn = None
BUFF_SIZE = 4096
buff = ''

orderid = 0
def request_mkt():
    global conn
    if conn:
        conn.send('[MKT]\n')
		orderid += 1
		conn.send("[ORD,A%03d,XXX/YYY,%9.5f, %9.5f]" % (orderid, random.random()*100., random.random()*100.))

sync_timer = PeriodicTimer(5, 0, request_mkt)

EXIT_DONE = 0
def exit_acts(*args):
    global EXIT_DONE
    if EXIT_DONE:
        return
    sync_timer.stop()
    print "Server Stoped"
    EXIT_DONE = 1

for sig in (signal.SIGABRT, signal.SIGILL, signal.SIGINT, signal.SIGSEGV, signal.SIGTERM):
    signal.signal(sig, exit_acts)


sync_timer.start()
try:
    while True:
        print "Accepting"
        conn, client_addr = SOCK.accept()
        print "Accepted"
        conn.send("[MKT]\n")

        try:
            while True:
                msg = conn.recv(BUFF_SIZE)
                buff += msg
                if buff:
                    print buff
                else:
                    break
        except KeyboardInterrupt:
            raise
        except socket.error as e:
            if e.errno != errno.ECONNRESET:
                raise
            print "Error Socket"
        finally:
            conn.close()
            conn = None
finally:
    exit_acts()







