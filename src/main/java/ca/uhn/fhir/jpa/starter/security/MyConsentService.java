package ca.uhn.fhir.jpa.starter.security;

import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.HumanName;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.*;


import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;


import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOutcome;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentContextServices;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentService;


public class MyConsentService implements IConsentService {
   /**
   * Invoked once at the start of every request
   */
   @Override
   public ConsentOutcome startOperation(RequestDetails theRequestDetails, IConsentContextServices theContextServices) {
      // This means that all requests should flow through the consent service
      // This has performance implications - If you know that some requests
      // don't need consent checking it is a good idea to return
      // ConsentOutcome.AUTHORIZED instead for those requests.
      System.out.println("METHOD NAME: startOperation");
      return ConsentOutcome.PROCEED;
   }

   /**
   * Can a given resource be returned to the user?
   */
   @Override
   public ConsentOutcome canSeeResource(RequestDetails theRequestDetails, IBaseResource theResource, IConsentContextServices theContextServices) {
      // In this basic example, we will filter out lab results so that they
      // are never disclosed to the user. A real interceptor might do something
      // more nuanced.


      if (theResource instanceof Patient) {
         Patient patient = (Patient)theResource;
         HumanName humanName = patient.getNameFirstRep();
         System.out.println("METHOD NAME: canSeeResource");
         System.out.println(humanName.getGivenAsSingleString());
         System.out.println(humanName.getFamily());
      }
      if (theResource instanceof Consent) {
         Consent consent = (Consent) theResource;
         String consentPatientId = consent.getPatient().getReferenceElement().getIdPart();
         System.out.println("ID OF PATIENT WHOS CONSENT THIS IS " + consentPatientId);

      }
      // Otherwise, allow the
      return ConsentOutcome.PROCEED;
   }

   
   /**
   * Modify resources that are being shown to the user
   */
   @Override
   public ConsentOutcome willSeeResource(RequestDetails theRequestDetails, IBaseResource theResource, IConsentContextServices theContextServices) {
      // Don't return the subject for Observation resources
      System.out.println("METHOD NAME: willSeeResource");
      // Removes lab results from 
      if (theResource instanceof Bundle) {
         System.out.println("Instance of Bundle");
         Bundle bundle = (Bundle)theResource;
         List<BundleEntryComponent> componentsList = bundle.getEntry();
         for (BundleEntryComponent component : componentsList){
               if (component.getResource().getResourceType() == ResourceType.Observation ){
                  Observation obs = (Observation)component.getResource();
                  if (obs.getCategoryFirstRep().hasCoding("http://terminology.hl7.org/CodeSystem/observation-category", "laboratory")) {
                     component.setResource(null);
                     return ConsentOutcome.REJECT;
                  }
               } 
         }
      }
         if (theResource instanceof Observation) {
            Observation obs = (Observation)theResource;
            if (obs.getCategoryFirstRep().hasCoding("http://terminology.hl7.org/CodeSystem/observation-category", "laboratory")) {
               return ConsentOutcome.REJECT;
            }
         }
      if (theResource instanceof Patient) {
        System.out.println("RESOURCE : Patient");

         Patient patient = (Patient)theResource;
         patient.setAddress(null);
         patient.setGender(null);
         patient.setContact(null);
         patient.setTelecom(null);
         patient.setBirthDate(null);
         patient.setDeceased(null);
      }
      if (theResource instanceof Observation) {
        System.out.println("RESOURCE : Observation");
         Observation obs = (Observation)theResource;
         obs.setSubject(null);
      }
      return ConsentOutcome.AUTHORIZED;
   }

   @Override
   public void completeOperationSuccess(RequestDetails theRequestDetails, IConsentContextServices theContextServices) {
      // We could write an audit trail entry in here
   }

   @Override
   public void completeOperationFailure(RequestDetails theRequestDetails, BaseServerResponseException theException, IConsentContextServices theContextServices) {
      // We could write an audit trail entry in here
   }


}
