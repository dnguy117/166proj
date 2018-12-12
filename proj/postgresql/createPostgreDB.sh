#! /bin/bash
echo "creating db named ... dnguy117_DB"
createdb -h localhost -p 9991 dnguy117_DB
pg_ctl status

echo "Copying csv files ... "
sleep 1
cp ../data/*.csv /tmp/dnguy117/myDB/data/.

echo "Initializing tables .. "
sleep 1
psql -h localhost -p 9991 dnguy117_DB < ../sql/create.sql
psql -h localhost -p 9991 dnguy117_DB < ../sql/function.sql