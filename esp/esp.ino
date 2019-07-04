#include <ESP8266WiFi.h>
#include <Servo.h>

// Replace these with your WiFi network settings
const char* ssid = "martes2"; //replace this with your WiFi network name
const char* password = "27272727"; //replace this with your WiFi network password

int a;
int last_value6, last_value7;

WiFiClient client;
char *server_ip = "192.168.188.100";

//see below
//char server_ip[20];   

Servo srvo;

void setup()
{
  srvo.attach(D4);
  
  pinMode(D3, OUTPUT);  // camera
  pinMode(D2, OUTPUT);  // alarm
  pinMode(D1, OUTPUT);  // sound

  pinMode(D0, OUTPUT);  // LED
  
  pinMode(D5, OUTPUT);
  pinMode(D6, INPUT_PULLUP);  // sensor1
  pinMode(D7, INPUT_PULLUP);  // sensor2
  
  a = 90;
  delay(1000);
  
  Serial.begin(115200);
  delay(1000);
  Serial.flush();

  // uncomment this code, if you prefer to enter IP address from Serial port
/*
  Serial.print("Enter server IP: ");
  
  char *p = server_ip;
  for (int i = 0; i < 4; i++)
  {
    int ip = read_number();
    p = print_num(p, ip);  
    if (i < 3) 
      p = print_char(p, '.');
  }
  Serial.println();
*/  
  connect_wifi();
  
  connect_server();
    
  last_value6 = digitalRead(D6);
  last_value7 = digitalRead(D7);
}

void connect_wifi()
{
  tone(D5, 1720);
  
  Serial.print("Wifi.begin()");
  WiFi.begin(ssid, password);

  Serial.println();
  Serial.print("Connecting");
  while (WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }
  tone(D5, 220);

  Serial.println("success!");
  Serial.print("IP Address is: ");
  Serial.println(WiFi.localIP());
}

void connect_server()
{
  delay(100);
  noTone(D5);

  Serial.print("connecting to ");
  Serial.print(server_ip);
  
  Serial.print(":");
  Serial.println(12345);
  
  if (!client.connect(server_ip, 12345))
  {
    Serial.println("Could not connect to the server.");
    while (1);
  }
  tone(D5, 440, 50); 
  digitalWrite(D0, HIGH);
  delay(500);
  digitalWrite(D0, LOW);  
}

int read_number()
{
  int x = 0;
  while (1) {
    if (Serial.available())
    {
      int c = Serial.read();
      Serial.write(c);
      if (c == 8) x /= 10;
      if ((c >= '0') && (c <= '9'))
        x = x * 10 + (c - '0');
      else return x;
    }
    delay(100);
  }
}

char *print_num(char *p, int num)
{
  if (num == 0) *(p++) = '0'; 
  else 
  {
    if (num < 0) { *(p++) = '-'; num = -num; }
    long q = 1;
    while (q <= num) q *= 10;
    while (q > 1)
    {
      q /= 10;
      *(p++) = (num / q) + '0';
      num %= q;
    }
  }
  *p = 0;
  return p;
}

char * print_char(char *p, int c)
{
  *(p++) = c;
  *p = 0;
  return p;
}

int read_and_process_packet(WiFiClient *client)
{
  char packet_type = client->read();
  switch (packet_type)
  {
    case 'a': a--; Serial.println(a); srvo.write(a); break;
    case 'q': a++; Serial.println(a); srvo.write(a); break;
    case 'F': //alarm
              digitalWrite(D2, HIGH); delay(400); digitalWrite(D2, LOW); 
              break;
    case 'D': //sound
              digitalWrite(D1, HIGH); delay(400); digitalWrite(D1, LOW); 
              break;  
    case 'C': //camera
              digitalWrite(D3, HIGH); delay(400); digitalWrite(D3, LOW); 
              break;
    case 'E': //sound and camera
              digitalWrite(D1, HIGH); delay(400); digitalWrite(D1, LOW);   
              delay(1600);
              digitalWrite(D3, HIGH); delay(400); digitalWrite(D3, LOW); 
              break;                
    case '-': return 0;
  }
  return 1;
}

void loop() 
{
  if (WiFi.status() != WL_CONNECTED)
  {
    connect_wifi();
    connect_server();
  }
  
  if (client.connected()) {
      Serial.println("Connected to server");

      while (client.connected())
      {
        if (digitalRead(D6) != last_value6)
        {
          client.write('A');
          last_value6 ^= 1;
        }
        
        if (digitalRead(D7) != last_value7)
        {
          client.write('A');
          last_value7 ^= 1;
        }
        
        if (Serial.available())
        {
          char c = Serial.read();
          client.write(c);
        }
        if (!read_and_process_packet(&client)) break;
        delay(1);
      }
      client.stop();
  }
  else
  {
    delay(1000);
    connect_server();
  }
  delay(1);
}
