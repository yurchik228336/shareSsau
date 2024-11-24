# ShareSsau - Social Network for SSAU Students

ShareSsau is a modern social networking platform specifically designed for students of Samara State Aerospace University (SSAU). It provides a secure and feature-rich environment for students to connect, share content, and engage with their university community.

## Features

- **User Authentication & Authorization**
  - Secure login and registration system
  - Role-based access control
  - Password encryption

- **User Profiles**
  - Customizable profile information
  - Profile picture and banner upload
  - Activity history

- **Content Sharing**
  - Create and share posts
  - Upload images and files
  - Interactive content feed

- **Performance Monitoring**
  - Real-time request tracking
  - Requests per second (RPS) monitoring
  - System resource usage statistics

- **Security Features**
  - HTTPS encryption
  - Rate limiting (10 requests per second per IP)
  - Cross-Site Request Forgery (CSRF) protection

## Technical Stack

- **Backend**
  - Java 17
  - Spring Boot 3.x
  - Spring Security
  - Spring Data JPA
  - MySQL Database

- **Frontend**
  - Thymeleaf templates
  - HTML5/CSS3
  - JavaScript

- **Security**
  - SSL/TLS encryption
  - Custom rate limiting implementation
  - Session management

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- MySQL Server
- SSL Certificate (for HTTPS)

### Configuration

1. Configure database connection in `application.properties`:
   ```properties
   spring.datasource.url=your_database_url
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

2. Configure SSL certificate:
   ```properties
   server.ssl.key-store=classpath:ssl/your-certificate.p12
   server.ssl.key-store-password=your-password
   server.ssl.key-store-type=PKCS12
   ```

3. Set up upload directory:
   ```properties
   upload-dir=uploads
   ```

### Building and Running

1. Clone the repository:
   ```bash
   git clone https://github.com/yurchik228336/shareSsau.git
   ```

2. Navigate to project directory:
   ```bash
   cd shareSsau
   ```

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   java -jar target/shareSsau-0.0.1-SNAPSHOT.jar
   ```

The application will be available at:
- HTTPS: https://localhost:8443
- HTTP: http://localhost:8081

## API Endpoints

- `/api/stats` - System performance metrics
- `/auth/**` - Authentication endpoints
- `/profile/**` - User profile management
- `/posts/**` - Post management

## Security Considerations

- The application implements rate limiting of 10 requests per second per IP
- All sensitive data is encrypted
- Passwords are hashed using secure algorithms
- Session tokens are managed securely

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- SSAU for inspiration and support
- All contributors who have helped with the project
