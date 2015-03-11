int incomingByte = 0;   // for incoming serial data
int led = 31;

void setup() {
  pinMode(led, OUTPUT); 
  Serial3.begin(9600);     // opens serial port, sets data rate to 9600 bps
}

void loop() {

  // send data only when you receive data:
  if (Serial3.available() > 0) {
    // read the incoming byte:
    incomingByte = Serial3.read();

    // say what you got:
    Serial3.print("I received: ");
    Serial3.println(incomingByte, DEC);

    switch (incomingByte) {
      case 48:
      digitalWrite(led, HIGH);
      break;
      case 49:
      digitalWrite(led, LOW); 
      break;
    }
  }
}