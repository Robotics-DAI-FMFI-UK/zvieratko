// connect the wake-up buzzer to pin D2
// arduino responds to check-connection packet '1' with '1'
// when it receives the buzzer packet '2', it sends 400ms pulse to pin D2
// uses 115200 baud rate

void setup() 
{
  Serial.begin(115200);
  pinMode(13, OUTPUT);
  pinMode(2, OUTPUT);
}

void loop() 
{
  if (Serial.available())
  {
    char c = Serial.read();
    switch (c)
    {
      case '1': Serial.print('1');
                break;
      case '2': digitalWrite(13, HIGH);
                digitalWrite(2, HIGH);
                delay(400);
                digitalWrite(2, LOW);
                digitalWrite(13, LOW);
                break;
      default: digitalWrite(13, HIGH);
               delay(1000);
               digitalWrite(13, LOW);
               Serial.print(c);
               break;
    }
  }
  delay(1);
}
