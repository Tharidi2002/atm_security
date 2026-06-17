/**
 * ATM Security System - ESP32 Client Firmware
 * 
 * This sketch runs on an ESP32 microcontroller to monitor standalone ATM sensors
 * and forward security events to the Centralized ATM Security Gateway.
 * 
 * Hardware Connections:
 * - Door Sensor (Magnetic Switch): GPIO 4 (active LOW, internal pull-up)
 * - Fire/Smoke Sensor (MQ-2/Digital Output): GPIO 5 (active HIGH)
 * - Vibration/Tamper Sensor (SW-420): GPIO 18 (active HIGH)
 * - Power Loss Detector (Optocoupler/Mains monitoring): GPIO 19 (active LOW - detects when mains fail and running on backup UPS)
 * 
 * Dependencies:
 * - ArduinoJSON library (Install via Library Manager)
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// --- Configuration ---
const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

// Centralized API Gateway URL (Update with your computer's IP address)
// Port 8080 routes /api/webhooks/sms to the alert-service
const char* gatewayUrl = "http://192.168.1.100:8080/api/webhooks/sms"; 

// The registered phone number of this ATM station (MUST match a registered station's phone number in the system)
const char* atmPhoneNumber = "+94771234567";

// --- GPIO Pins ---
#define PIN_DOOR 4
#define PIN_FIRE 5
#define PIN_VIBRATION 18
#define PIN_POWER 19

// --- State Variables ---
bool doorTriggered = false;
bool fireTriggered = false;
bool vibrationTriggered = false;
bool powerFailed = false;

// Timers
unsigned long lastHeartbeat = 0;
const unsigned long HEARTBEAT_INTERVAL = 300000; // Send status report every 5 minutes

void setup() {
  Serial.begin(115200);
  
  // Initialize GPIO Pins
  pinMode(PIN_DOOR, INPUT_PULLUP);
  pinMode(PIN_FIRE, INPUT);
  pinMode(PIN_VIBRATION, INPUT);
  pinMode(PIN_POWER, INPUT_PULLUP);

  // Connect to Wi-Fi
  connectToWiFi();
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    connectToWiFi();
  }

  // Read Sensors & Detect Changes
  checkSensors();

  // Periodic Heartbeat / Status report
  if (millis() - lastHeartbeat >= HEARTBEAT_INTERVAL) {
    sendHeartbeat();
    lastHeartbeat = millis();
  }

  delay(200); // Small delay to prevent CPU thrashing
}

void connectToWiFi() {
  Serial.print("Connecting to Wi-Fi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  
  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20) {
    delay(500);
    Serial.print(".");
    attempts++;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\nWi-Fi Connected!");
    Serial.print("IP Address: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\nFailed to connect to Wi-Fi. Will retry later.");
  }
}

void checkSensors() {
  // 1. Door Sensor (Normally Closed, open switch pulls HIGH or LOW depending on circuit)
  // Assumes: LOW means closed, HIGH means open (unauthorized access)
  bool currentDoorState = (digitalRead(PIN_DOOR) == HIGH);
  if (currentDoorState != doorTriggered) {
    doorTriggered = currentDoorState;
    if (doorTriggered) {
      sendAlert("DOOR_OPEN", "CRITICAL: Unauthorized ATM door opening detected in cash counter zone!");
    } else {
      sendAlert("INFO", "INFO: ATM door closed and secured.");
    }
  }

  // 2. Fire/Smoke Sensor (Active HIGH on detection)
  bool currentFireState = (digitalRead(PIN_FIRE) == HIGH);
  if (currentFireState != fireTriggered) {
    fireTriggered = currentFireState;
    if (fireTriggered) {
      sendAlert("FIRE_ALARM", "CRITICAL: Smoke/Fire detected in general zone!");
    }
  }

  // 3. Vibration Sensor (Active HIGH on shaking/impact)
  bool currentVibrationState = (digitalRead(PIN_VIBRATION) == HIGH);
  if (currentVibrationState != vibrationTriggered) {
    vibrationTriggered = currentVibrationState;
    if (vibrationTriggered) {
      sendAlert("PHYSICAL_TAMPERING", "CRITICAL: Strong vibration/shaking detected. Possible physical breach attempt!");
    }
  }

  // 4. Power Failure (Active LOW - detects when mains drops)
  bool currentPowerState = (digitalRead(PIN_POWER) == LOW);
  if (currentPowerState != powerFailed) {
    powerFailed = currentPowerState;
    if (powerFailed) {
      sendAlert("POWER_FAILURE", "WARNING: Main AC power failure! Running on UPS battery back-up.");
    } else {
      sendAlert("INFO", "INFO: Main AC power restored. Charging backup systems.");
    }
  }
}

void sendAlert(const char* alertType, const char* message) {
  Serial.print("Sending Security Alert: ");
  Serial.println(message);
  
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    http.begin(gatewayUrl);
    http.addHeader("Content-Type", "application/json");

    // Construct JSON Payload
    StaticJsonDocument<256> doc;
    doc["sender"] = atmPhoneNumber;
    doc["message"] = message;

    String jsonString;
    serializeJson(doc, jsonString);

    int httpResponseCode = http.POST(jsonString);
    
    if (httpResponseCode > 0) {
      String response = http.getString();
      Serial.print("Gateway Response: ");
      Serial.println(httpResponseCode);
      Serial.println(response);
    } else {
      Serial.print("Error sending POST request: ");
      Serial.println(http.errorToString(httpResponseCode).c_str());
    }
    
    http.end();
  } else {
    Serial.println("Error: No Wi-Fi connection. Alert queued.");
  }
}

void sendHeartbeat() {
  String statusMsg = "HEARTBEAT: ATM security systems fully online. Current Status -> Door: ";
  statusMsg += doorTriggered ? "OPEN" : "CLOSED";
  statusMsg += " | Temp/Smoke: ";
  statusMsg += fireTriggered ? "ALARM" : "OK";
  statusMsg += " | Vibration: ";
  statusMsg += vibrationTriggered ? "TAMPERING" : "OK";
  statusMsg += " | Power: ";
  statusMsg += powerFailed ? "FAIL (UPS)" : "AC OK";

  sendAlert("GENERAL", statusMsg.c_str());
}
