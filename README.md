## Deployment
```
docker build -t hexfields-dominion-api .
docker run -p 8080:8080 -e APP_LOBBYMANAGER_INITIALCAPACITY=50 hexfields-dominion-api
```

## Pipeline Status

[![Java CI with Gradle](https://github.com/Hexfields-Studio/HexfieldsDominion-Backend/actions/workflows/gradle.yml/badge.svg)](https://github.com/Hexfields-Studio/HexfieldsDominion-Backend/actions/workflows/gradle.yml)
