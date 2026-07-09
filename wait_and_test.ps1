$i=0
$max=30
$uri='http://localhost:8080/api/alerts/pending/count'
while ($i -lt $max) {
  try {
    $r = Invoke-WebRequest -Uri $uri -UseBasicParsing -Method GET -ErrorAction Stop
    Write-Output "SERVER_UP"
    break
  } catch {
    Start-Sleep -Seconds 1
    $i = $i + 1
  }
}
if ($i -ge $max) { Write-Output "SERVER_NOT_UP"; exit 1 }
Write-Output "Running tests..."
$hb = Invoke-RestMethod -Uri 'http://localhost:8080/api/alerts/heartbeat' -Method POST -Body (@{atmCode='ALARM-Z8B-01';simNumber='0764646725'} | ConvertTo-Json) -ContentType 'application/json' -ErrorAction SilentlyContinue
Write-Output "HEARTBEAT_RESPONSE:"
$hb | ConvertTo-Json -Depth 5
$sms = Invoke-RestMethod -Uri 'http://localhost:8080/api/alerts/sms-simulate' -Method POST -Body (@{simNumber='0764646725';message='Integration test message';atmCode='ALARM-Z8B-01'} | ConvertTo-Json) -ContentType 'application/json' -ErrorAction SilentlyContinue
Write-Output "SMS_RESPONSE:"
$sms | ConvertTo-Json -Depth 5
