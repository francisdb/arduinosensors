//TMP36 Pin Variables
//the analog pin the TMP36′s Vout (sense) pin is connected to
//the resolution is 10 mV / degree centigrade
//(500 mV offset) to make negative temperatures an option
int temperaturePin = 0; 

//the analog pin the photoresistor is
//connected to the photoresistor is not calibrated to any units so
//this is simply a raw sensor value (relative light)
int lightPin = 5;

int ledPin = 13;

/*
 * setup() – this function runs once when you turn your Arduino on
 * We initialize the serial connection with the computer
 */
void setup()
{
  pinMode(ledPin, OUTPUT);
  Serial.begin(9600);
}

void loop()
{
  ledOn();
  
  float temperature = getTemperature();
  
  Serial.print("temp\t");
  Serial.print(temperature);
  Serial.print("\t");
  
  int lightLevel = getLightLevel();
  
  Serial.print("light\t");
  Serial.println(lightLevel);
  
  delay(200);
  ledOff();
  delay(3000);
}

void ledOn(){
  digitalWrite(ledPin, HIGH);
}

void ledOff(){
  digitalWrite(ledPin, LOW);
}

int getLightLevel(){
  int lightLevel = analogRead(lightPin);
  //adjust the value 0 to 900 to span 0 to 255
  lightLevel = map(lightLevel, 0, 900, 0, 255);
  //make sure the value is between 0 and 255
  lightLevel = constrain(lightLevel, 0, 255);
  return lightLevel;
}

float getTemperature(){
  float temperature = 0;
  temperature = getVoltage(temperaturePin);
  temperature = (temperature - 0.5) * 100;
  return temperature;
}

/*
* getVoltage() – returns the voltage on the analog input defined by pin
*/
float getVoltage(int pin){
  //converting from a 0 to 1023 digital range
  // to 0 to 5 volts (each 1 reading equals ~ 5 millivolts
  return (analogRead(pin) * .004882814); 
}
