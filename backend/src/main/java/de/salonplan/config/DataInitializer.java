package de.salonplan.config;

import de.salonplan.employee.Employee;
import de.salonplan.employee.EmployeeRepository;
import de.salonplan.salonservice.SalonService;
import de.salonplan.salonservice.SalonServiceRepository;
import de.salonplan.user.AppUser;
import de.salonplan.user.AppUserRepository;
import de.salonplan.user.UserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class DataInitializer implements ApplicationRunner {
    private final AppUserRepository users;
    private final EmployeeRepository employees;
    private final SalonServiceRepository services;
    private final PasswordEncoder encoder;
    private final BootstrapProperties properties;

    public DataInitializer(
            AppUserRepository users,
            EmployeeRepository employees,
            SalonServiceRepository services,
            PasswordEncoder encoder,
            BootstrapProperties properties
    ) {
        this.users = users;
        this.employees = employees;
        this.services = services;
        this.encoder = encoder;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createUserIfMissing(
                properties.adminEmail(),
                properties.adminPassword(),
                UserRole.ADMIN
        );
        AppUser employeeUser = createUserIfMissing(
                properties.employeeEmail(),
                properties.employeePassword(),
                UserRole.EMPLOYEE
        );

        if (employees.count() == 0) {
            Employee employee = new Employee("Anna", "Beispiel", properties.employeeEmail());
            employee.linkUser(employeeUser);
            employees.save(employee);
        }

        if (services.count() == 0) {
            services.save(new SalonService(
                    "Damenhaarschnitt",
                    "Waschen, schneiden und föhnen",
                    60,
                    new BigDecimal("49.00")
            ));
            services.save(new SalonService(
                    "Herrenhaarschnitt",
                    "Waschen und schneiden",
                    30,
                    new BigDecimal("29.00")
            ));
        }
    }

    private AppUser createUserIfMissing(String email, String password, UserRole role) {
        return users.findByEmailIgnoreCase(email).orElseGet(() ->
                users.save(new AppUser(email, encoder.encode(password), role))
        );
    }
}