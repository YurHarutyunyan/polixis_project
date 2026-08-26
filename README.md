# Polixis Company Search

Scrapes UK Companies House search results and company details, caches them in MongoDB.

## Run it

Requires Docker and Docker Compose.

Before running, make sure ports `8081` (Spring Boot) and `27017` (MongoDB) are not already in use on your machine.

```
git clone <this repo>
cd polixis
docker compose up --build
```

This starts the backend on `http://localhost:8081` and a MongoDB instance alongside it.

Once it's up, open `index.html` directly in a browser (double-click it, or `open index.html` / `xdg-open index.html`), type a search term, and click Search.

Notes:
- The first search for a given term does a live scrape and can take several minutes (by default it parses up to 100 matching companies, each requiring its own detail/officers/PSC page fetches — see the comment in `CompanySearchService.search()` if you want to lower that number for faster results).
- Repeating the same search within a minute returns instantly from the MongoDB cache instead of re-scraping.
- Data persists across `docker compose down` / `up` via a named volume (`mongo-data`).

## Run it without Docker

Requires a local MongoDB instance and Java 21 + Maven.

```
mvn spring-boot:run
```

By default this connects to `mongodb://admin:admin@localhost:27017/polixis?authSource=admin` — override with the `SPRING_DATA_MONGODB_URI` environment variable if your local Mongo is set up differently.
