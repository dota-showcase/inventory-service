# Dota Showcase - Inventory Service

REST API service to store and track changes of steam user's dota 2 inventory items.

## Features

* Inventory history - created, updated, deleted items
* Rate limiter

## Getting Started

### Prerequisites

- Java 17
- Apache Maven
- MongoDB 5.0

### Installation

1. Create `.env` based on `.env.example`
2. Create `environment.properties` based on `environment.properties.example` in `app/src/main/resources`
3. Set the steam api key property to access Steam's API:
    ```bash
    env.steam.api.key=your_api_key
    ```
4. To run in development environment. 
   You can use a default build for IntelliJ IDEA to run the app - `.run/dev-build.run.xml`.
    ```bash
    docker-compose up
    ```
5. To run in production environment
    ```bash
    docker compose -f docker-compose.prod.yml up -d
    ```

## Resources

- [Steam API - GetPlayerItems](https://wiki.teamfortress.com/wiki/WebAPI/GetPlayerItems)

## License

The GNU General Public License v3.0. Please see [License File](LICENSE) for more information.