package com.hiring4u.security;
import com.hiring4u.entity.CanEntity;
import com.hiring4u.entity.RecEntity;
import com.hiring4u.repository.RegistrationRepository;
import com.hiring4u.repository.RecruitorsRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



import java.util.Optional;
import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final RegistrationRepository candidateRepo;
    private final RecruitorsRepository recruiterRepo;

    public CustomUserDetailsService(RegistrationRepository candidateRepo, RecruitorsRepository recruiterRepo) {
        this.candidateRepo = candidateRepo;
        this.recruiterRepo = recruiterRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Optional<CanEntity> candidate = candidateRepo.findByEmail(normalizedEmail);
        if (candidate.isPresent()) {
            CanEntity user = candidate.get();
            return org.springframework.security.core.userdetails.User
                    .builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        }

        Optional<RecEntity> recruiter = recruiterRepo.findByEmail(normalizedEmail);
        if (recruiter.isPresent()) {
            RecEntity user = recruiter.get();
            return org.springframework.security.core.userdetails.User
                    .builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();
        }

        throw new UsernameNotFoundException("User not found");
    }
}
