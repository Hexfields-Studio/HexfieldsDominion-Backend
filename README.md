## Deployment
```
docker build -t hexfields-dominion-api .
docker run -p 8080:8080 -e APP_LOBBYMANGER_INITIALCAPACITY=50 hexfields-dominion-api
```