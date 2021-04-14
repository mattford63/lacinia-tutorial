#!/usr/bin/env bash

docker exec --env PGPASSWORD=lacinia -it cgg_db_1 psql -Ucgg_role -d cggdb
