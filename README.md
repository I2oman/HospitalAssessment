# Hospital management system

This project is designed to develop a **hospital database application** for a mid-size health insurance company. The
system manages health claims and stores essential data for:

1. **Patients**: Includes name, address, phone, email, primary care doctor, insurance ID, and insurance company name.
2. **Doctors**: Tracks doctor specialties, contact information, and addresses.
3. **Visits and Prescriptions**: Maintains records of patient visits and prescriptions, including drug details (name,
   purpose, side effects). Ensures prescriptions are checked for conflicts with claim eligibility.

The application will initially focus on claims processing while offering future potential for trend analysis and
predictive modeling. It will only support English at this time.

## Technologies

The following technologies are used in the development of this application:

- **Backend**: Java 23+, JavaFX
- **Database**: MySQL or similar relational database system
- **Build Tools**: Gradle and Maven (optional)
- **Environment Configuration**: `.env` files for sensitive credential storage
- **IDE Recommended**: IntelliJ IDEA Ultimate Edition

## Requirements

Before running the project, ensure you have the following installed on your machine:

- Java Development Kit (JDK) version 23 or higher
- IntelliJ IDEA (Recommended: Ultimate Edition)

## Getting Started

Follow these steps to run the project on your local machine:

### Clone the Repository

```bash
git clone <repository-url>
cd <repository-folder>
```

### Set Up Environment Variables

Create a `.env` file in the root directory of the project. Here is an example `.env` file:

```plaintext
DATABASE_URL=jdbc:mysql://localhost:3306/db_name
DATABASE_USERNAME=db_user
DATABASE_PASSWORD=db_password
```

Make sure to replace the placeholder values with your actual database configuration.
