# A/B loadbalancer

This is simple REST application that is able to route traffic to different backends depending on a group that user falls into. This can be used to conduct A/B tests on a group of users.

## API

HTTP GET ``/route?{id}``, returns

* 200 on success
* 400 if no ``id`` was set  

Example:

    curl http://localhost:8080/route?id=abc

## Running

By default server starts on port ``8080`` and binds to ``0.0.0.0``, this can be changed by setting ``host`` and ``port`` environment variables.

Application runs in one of two modes:

* Raw Undertow handler,
* Spring Boot on Undertow.

Run using gradle:

    ./gradlew runUndertow

or

    ./gradlew runBoot

## Configuration

Configuration is read from yaml file ``config.yml``. Configuration constrains:

* every weight is >= 0;
* sum of weights is 100.

Sample confing file:

    - name: A
      weight: 40
    - name: B
      weight: 60

## Solution

System prepares group distribution based on group probabilities defined in configuration file. For each request user id is being hashed, result group is selected from distribution array based on hash value (hash value mod len(distribution)). Hashing is based on murmur hash 3 algorithm.

## Benchmark

The real reason behind this project is measuring what is the cost of abstractions provided by Spring. In addition benchmark checks what is the best io / worker thread balance in this case.

For benchmarking Google Caliper and OkHttp are used. Benchmarking code borrows a lot from [OkHttp benchmarks](https://github.com/square/okhttp/tree/master/benchmarks).


### Sync / Asyc

At first a classic async I/O approach was used and once request was read it was passed to worker. This gave results around 27k r/s [see benchmark](https://microbenchmarks.appspot.com/runs/7f9a5752-8b3e-4b61-9b3e-caec90e09496). One may observe that in the benchmark the processing speed is not related to number of ``wokerThreads`` but rather ``ioThreads``. This resulted in switch to sync processing and better performance.

### Results

System is able to handle slightly more than 30k requests per second on my Intel i5 laptop [see benchmark](https://microbenchmarks.appspot.com/runs/4cad93b8-7192-429f-b6e1-ade4dae338cb).

Using Spring is around 9k request per second.

### Unexpected findings

* Undertow 1.3 rc2 is consistently slower then 1.1 final in the benchmark approx. 27k r/s vs. 30k r/s
* Spring Boot rest handler using Callable<String> is unbelievably slow approx. 600 r/s while sync impl. rates approx. 9k r/s
