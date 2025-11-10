## Deployment
```
docker build -t hexfields-dominion-api .
docker run -p 8080:8080 -e APP_LOBBYMANAGER_INITIALCAPACITY=50 hexfields-dominion-api
```