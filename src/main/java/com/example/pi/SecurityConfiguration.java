package com.example.pi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    UserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
               .antMatchers("/customers/DisplayAllCustomers","/customers/FindByCustomerID/{id}","/customers/FilterCustomersByIDPoof/{id_type}","/customers/AddCustomers","/customers/DeleteCustomer/{id}").hasRole("ADMIN")
                .antMatchers("/customers/UpdateCustomer","/customers/Ministatement/{id}","/customers/CheckBalance/{id}","/customers/CheckTransactionMode/{mode}").hasAnyRole("ADMIN", "USER")
                .antMatchers("/customers/AllTransactionsHistory","/customers/LatestTransactions","/customers/Banking").permitAll()
                .and().formLogin();
    }

	@Bean
    public PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}