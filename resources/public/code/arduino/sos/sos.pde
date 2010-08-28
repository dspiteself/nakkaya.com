int redLEDPin = 10;
int yellowLEDPin = 9;

int shortPulse = 250;
int longPulse = 125;
int letterDelay = 1000;

//letters 0 for short pulse 1 for long
int letterS[] = {0, 0, 0};
int letterO[] = {1, 1, 1};

void setup(){
  pinMode(redLEDPin, OUTPUT);
  pinMode(yellowLEDPin, OUTPUT);
}

void redLed(int time){
    digitalWrite(redLEDPin, HIGH);
    delay(time);
    digitalWrite(redLEDPin, LOW);
    delay(time);
}

void yellowLed(){
    digitalWrite(yellowLEDPin, HIGH);
    delay(2000);
    digitalWrite(yellowLEDPin, LOW);
}

void blinkLetter(int* letter){
  for(int i=0; i<3; i++){
    int val = letter[i];

    if(val == 0)
      redLed(shortPulse);
    else
      redLed(longPulse);
  }
  delay(letterDelay);
}

void loop() { 
  blinkLetter(letterS);
  blinkLetter(letterO);
  blinkLetter(letterS);

  yellowLed();
} 
