# csv2mail
A simple java application to parse CSV files, group by email address, and send selected data in the email body and/or as an attachment to the given email address.

## Getting Started

### Prerequisites
* maven
* java (8 or higher)

### Installing

* Download or clone

```bash
git clone https://github.com/csyperski/csv2mail .
```

* Build
```bash
mvn package
``` 

### Usage

You need three files for this application to function:

* a configure file (docs/csv2email.properties)
* a source csv file (docs/csv2email.csv)
* a HTML email template (docs/csv2email.html)

You'll need to configure the csv2email.properties file to include you SMTP information at a minimum to test

The application is executed with the following command

```bash
java -jar csv2mail.jar config-file-path html-template-path csv-path
```

## Authors

* **Charles Syperski** - [csyperski](https://github.com/csyperski)

