import argparse
import socket
import atexit
import time
from multiprocessing import Process
from messages import Token


class TokenRingUser:
    def __init__(self, address, port, neighbor_address, neighbor_port, protocol):
        self.address = address
        self.port = int(port)
        self.neighbor_address = neighbor_address
        self.neighbor_port = int(neighbor_port)
        if protocol == 'udp':
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        elif protocol == 'tcp':
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.socket.bind(('', self.port))
        if protocol == 'tcp':
            self.socket.listen(2)
            self.connection = None
        self.has_token = False
        self.protocol = protocol
        self.logger_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

    def send(self, token):
        if token.category == 'TEXT':
            self.has_token = False
        if self.protocol == 'udp':
            self.socket.sendto(bytes(str(token), 'cp1250'), (self.neighbor_address, self.neighbor_port))
        elif self.protocol == 'tcp':
            self.socket.send(bytes(str(token), 'cp1250'))

    def send_message(self, timer_process):
        dest_address = input("Enter destination IP: ")
        dest_port = input("Enter destination port: ")
        text = input("Enter message: ")
        timer_process.terminate()
        self.send(Token(self.address, self.port, dest_address, dest_port, text, 'TEXT'))

    def send_connect(self):
        if self.protocol == 'tcp':
            self.socket.connect((self.neighbor_address, self.neighbor_port))
        self.send(Token(self.address, self.port, self.neighbor_address, self.neighbor_port, '', 'CONNECT'))

    def send_disconnect(self):
        if self.protocol == 'tcp':
            self.socket.close()
        self.send(Token(self.address, self.port, self.neighbor_address, self.neighbor_port, '', 'DISCONNECT'))

    def send_config(self, token):
        self.send(Token(token.src_address, token.src_port, self.neighbor_address, self.neighbor_port,
                        '{}:{}'.format(self.address, str(self.port)), 'CONFIG'))

    def pass_config(self, token):
        self.send(
            Token(token.src_address, token.src_port, self.neighbor_address, self.neighbor_port, token.text, 'CONFIG'))

    def receive_token(self):
        if self.protocol == 'udp':
            buffer, address = self.socket.recvfrom(1024)
        elif self.protocol == 'tcp':
            buffer = self.connection.recv(1024)
        time.sleep(1)
        print('{}:{} received {}'.format(self.address, self.port, str(buffer, 'cp1250')))
        self.logger_socket.sendto(bytes(('{}:{} received {}'.format(self.address, self.port, str(buffer))), 'cp1250'),
                                  ('229.0.0.0', 9009))
        token = bytes_to_token(buffer)
        if token.category == 'TEXT':
            if token.dest_address == self.address and token.dest_port == self.port:
                self.has_token = True
            elif token.src_address == self.address and token.src_port == self.port:
                self.has_token = True
                print("No destination found with given address and port")
            else:
                self.send(token)
        elif token.category == 'CONNECT':
            if token.dest_address == self.address and token.dest_port == self.port:
                self.neighbor_address = token.src_address
                self.neighbor_port = int(token.src_port)
            else:
                self.send_config(token)
        elif token.category == 'CONFIG':
            config_sender = token.text.split(':')
            if self.neighbor_address == config_sender[0] and self.neighbor_port == int(config_sender[1]):
                self.neighbor_address = token.src_address
                self.neighbor_port = int(token.src_port)


def bytes_to_token(buffer):
    buf = str(buffer).split(' ')
    for i in range(0, len(buf)):
        buf[i] = buf[i].strip('\\').strip('\'').strip('\"')
    category = buf[1]
    src = buf[4].split(':')
    dest = buf[6].split(':')
    text = buf[len(buf) - 1]
    return Token(src[0], int(src[1]), dest[0], int(dest[1]), text, category)


def timer(user):
    t = 0
    while t < 30:
        time.sleep(1)
        t += 1
    user.has_token = False
    user.send(Token(user.address, user.port, user.neighbor_address, user.neighbor_port, 'timeout', 'TEXT'))
    exit()


def run():
    parser = argparse.ArgumentParser()
    parser.add_argument('user_id', help='user string id')
    parser.add_argument('port_number', help='user port number')
    parser.add_argument('neighbor_ip', help='neighbor\'s IP used to pass messages')
    parser.add_argument('neighbor_port', help='neighbor\'s port used to pass messages')
    parser.add_argument('has_token', choices=['token', 'notoken'],
                        help='specifies if user will initially have token (only one token allowed)')
    parser.add_argument('protocol', choices=['tcp', 'udp'], help='communication protocol')
    args = parser.parse_args()
    user = TokenRingUser('127.0.0.1', args.port_number, args.neighbor_ip, args.neighbor_port, args.protocol)
    if args.has_token == "token":
        user.has_token = True
    if not user.has_token and user.protocol == 'tcp':
        user.connection, addr = user.socket.accept()
    atexit.register(user.send_disconnect)
    p = Process(target=timer, args=(user,))
    while True:
        if user.has_token:
            p.start()
            user.send_message(p)
        else:
            if user.protocol == 'tcp':
                user.connection, addr = user.socket.accept()
            user.receive_token()


if __name__ == '__main__':
    run()
