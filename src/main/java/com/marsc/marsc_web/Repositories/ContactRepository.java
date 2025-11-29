package com.marsc.marsc_web.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;


import com.marsc.marsc_web.Entities.Contact;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
}
