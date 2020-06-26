import socket

# Target IP address of android device
HOST = '192.168.1.35'
PORT = 45104

pcIP = socket.gethostbyname(socket.gethostname())
print("PC IP: " + pcIP)
HOST = pcIP #garanticilik

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) #SOCK_STREAM
s.bind((HOST,PORT))
print("binded...")
s.listen(1) #en fazla 1 baglanti kabul et

print("listening...")
conn, addr = s.accept()

print("connected by " + addr[0])
print(f'the connection variable= {conn}\n')

while 1:
    print("veri yazdirma basladi...")
    data = conn.recv(1024).decode()
    print("mesaj alindi")
    if not data:
        print("veri bos")
        break
    print("data: " + data)
    #conn.sendall(data)
    conn.close()
    print("baglanti sonlandirildi")

print("donguden cikildi")
