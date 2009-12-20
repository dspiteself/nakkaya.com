const int boardheight = 400;
const int p1Potpin = 0;
const int p2Potpin = 1;

void setup(){
  Serial.begin(9600);
}

void loop(){

  int p1 = analogRead(p1Potpin);
  int p2 = analogRead(p2Potpin);

  Serial.print(map(p1,0,1024,0,400));
  Serial.print(":");
  Serial.println(map(p2,0,1024,0,400));

  delay(50);
}
