
# soft-drinks-industry-levy-variations-frontend

## About
Soft Drinks Industry Levy variations frontend is a microservice that is part of the Soft Drinks Industry Levy service.

The variations journey allows users to:
- Correct an error in a previous return
- Update contact, packaging site or warehouse details
- Change business activity
- Cancel SDIL registration


The Soft Drinks Industry Levy (SDIL) digital service is split into a number of different microservices all serving specific functions which are listed below:

**Liability tool** - Standalone frontend service that is used to check a company's liability in regards to the levy.
**Registration Frontend** - The initial subscription registration service.
**Returns Frontend** - The returns journey frontend for the service.
**Variations Frontend** - Service to submit variations on registration and returns functionalities.
**Accounts Frontend** - Dashboard functionality service.
**Backend** - The service that the frontend uses to call HOD APIs to retrieve and send information relating to business information and subscribing to the levy.
**Stub** - Microservice that is used to mimic the DES APIs when running services locally or in the development and staging environments.

For details about the sugar tax see [the GOV.UK guidance](https://www.gov.uk/guidance/soft-drinks-industry-levy)

## Running from source
Clone the repository using SSH:

`git@github.com:hmrc/soft-drinks-industry-levy-variations-frontend.git`

If you need to setup SSH, see [the github guide to setting up SSH](https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/)

Run the code from source using

`./run.sh`

Run other services required for running this service via the service manager. (*You need to be on the VPN*)
`sm2 --start SDIL_ALL`

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").