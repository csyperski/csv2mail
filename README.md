# csv2mail
A simple java application to parse CSV files, group by email address, and send selected data in the email body and/or as an attachment to the given email address.

## Getting Started

![Overview](https://www.cwssoft.com/wp-content/uploads/2018/10/csv2mail.png)

Csv2mail takes a CSV file with a column containing an email address, groups the data by the email address and sends the subset of data to the grouped email address.  Csv2mail can attach the resulting subset of data as a CSV file or expose it to an HTML template.
  

### Prerequisites
* maven
* java (8 or higher)

### Installing

#### Download or clone

```bash
git clone https://github.com/csyperski/csv2mail 
```

#### Build
```bash
mvn package
``` 

### Usage

You need three files for this application to function:

* a configure file (docs/csv2mail.properties)
* a source csv file (docs/csv2mail.csv)
* a HTML email template (docs/csv2mail.html)

You'll need to configure the csv2email.properties file to include your SMTP connection information to get started.

The application is executed with the following command:

```bash
java -jar csv2mail.jar config-file-path html-template-path csv-path
```

## Authors

* **Charles Syperski** - [csyperski](https://github.com/csyperski)

