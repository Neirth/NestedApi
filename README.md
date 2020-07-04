<p align="center">
  <img src="./docs/assets/NestedApi-logo.svg?raw=true">
</p>

Microservices based framework to create a backend services for the real world. (Under Heavy Development)

## Deployment 
This project is a Github Template, is to say, you can use this project for create a real backend service, only you need write the modules that you need. The modules they can use other programming language than Java, as long as it is compatible with the technologies that use the modules already available. Consult the documentation for more information.

## Why is project?
This project aims to facilitate the times throughout the development cycle, allowing you to focus on the components that you really need to develop with your team. For this, despite the fact that this project is part of its modules written in Java 8, there is complete freedom to write your own modules in the programming language you want, as well as using your own components per module. We will focus on improving our modules so that they fulfill their objective correctly.

## How works this project?
This project converts the endpoints of a RESTful API into components dedicated to a single purpose, thus allowing us to scale the components of our service to support large workloads.

Next, we will explain graphically how this project is composed:

![NestedApiDiagram](./docs/assets/NestedApi-Diagram.svg?raw=true)

In the diagram we can see how the services would be structured. Each service can have replicas and work with each other in a coordinated way. This is due to our api gateway, which also acts as a load balancer and the messaging service, which is based on RabbitMQ and the pub/sub model. Each module will be in charge of a particular task, in case another module needs to access the information of the original module, it will produce a message that will be later consumed by the module that provides that service. 

Regarding the database, each module will be responsible for loading only the tables it needs to operate, and mustn't access the information managed by other modules. With this we guarantee that the information is only manipulated by the module in charge of processing said information.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
You need a host with Linux and Docker support, also need the utility docker-compose to deploy all containers of the project.

### Installing
Only you need deploy with docker compose:
> ``
operator@nested-host: ~$ docker-compose -f docker-compose.yml up -d
``

## Built with
* Apache Avro - For serialize RPC petitions in a binary format.
* RabbitMQ - For made a internal bus for RPC petitions between services.
* Quarkus Framework - Used for run the code from the module project, and gets the native build.
* Docker - For package the project and the dependencies inside a OCI contanier.
* Ambassador - For centralize all microservice endpoints into single API URL using API Gateway Concept.
* PostgreSQL - Used for provide a high-performance database for users module.
* MongoDB - Used for provide a high-performance database for invoices module.

## Contributing 
Please read [CONTRIBUTING.md](./CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Join us into the community!
This project not only stays in this repository, but also that there is a community behind willing to support it. Join and enjoy a good time with us!

[![Discord](https://img.shields.io/discord/726426880823001248?label=Community&logo=discord&logoColor=ffffff&color=grey&labelColor=6A7EC2&style=for-the-badge)](https://discord.gg/t5D7PHz)

## Sponsor the project 
If the project has helped you deploy your service, you could leave a donation as a token of appreciation. Well, doing this job only an arduous task.

## License
This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.
