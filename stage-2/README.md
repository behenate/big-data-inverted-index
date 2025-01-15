# How to run in docker
## If you want to run the crawler:
- `docker compose build crawler`
- `docker compose up crawler`

Same for any other service
Running any service should start the `mongo_db` service. If it doesn't work for somer reason 
you can just run

- `docker compose build mongo_db`
- `docker compose up mongo_db`