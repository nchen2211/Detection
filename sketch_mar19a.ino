#include <SoftwareSerial.h>
///*
//  ReadAnalogVoltage (for muscle sensor)
//  Reads an analog input on pin 0, converts it to voltage, and prints the result to the serial monitor.
//  Graphical representation is available using serial plotter (Tools > Serial Plotter menu)
//  Attach the center pin of a potentiometer to pin A0, and the outside pins to +5V and ground.
//  This example code is in the public domain.
//*/

// for bluetooth
int bluetoothTx = 11;
int bluetoothRx = 10;

SoftwareSerial bluetooth(bluetoothRx, bluetoothTx);


// the setup routine runs once when you press reset:
void setup() {
  // initialize serial communication at 9600 bits per second:
  Serial.begin(9600);

  // setup bluetooth serial connection to android
  bluetooth.begin(115200);
  bluetooth.print("$$$");
  delay(500);
  bluetooth.println("U,9600,N");
  bluetooth.begin(9600);
}

// the loop routine runs over and over again forever:
void loop() {
  // read the input on analog pin 0:
  int sensorValue = analogRead(A0);
  // Convert the analog reading (which goes from 0 - 1023) to a voltage (0 - 5V):
  float voltage = sensorValue * (5.0 / 1023.0);
  // print out the value you read:
  Serial.println(sensorValue);

   // read from bluetooth and write to usb serial
  if (bluetooth.available()){
    bluetooth.print(sensorValue);
    bluetooth.print("~");
    delay(500);
  }

  // Read from usb serial to bluetooth
  if (Serial.available()){  // if data is available to read
    Serial.println("DATA AVAILABLE");
    char toSend = (char)Serial.read();
    bluetooth.print(toSend);
  }
}
