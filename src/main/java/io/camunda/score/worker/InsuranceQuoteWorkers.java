package io.camunda.score.worker;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class InsuranceQuoteWorkers {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceQuoteWorkers.class);
    private static final Random random = new Random();

    // Possible values
    private static final String[] VEHICLE_TYPES = {"sedan", "suv", "truck", "motocycle"};
    private static final String[] VEHICLE_COLORS = {"black", "white"};
    private static final String[] BRANDS = {"CamundaInsurance", "BestInsuranceNA"};
    private static final Boolean[] APPROVED_VALUES = {true, false};

    @JobWorker(type = "vehicle_info_lookup", autoComplete = false)
    public void vehicleInfoLookup(final JobClient client, final ActivatedJob job,
                                  @Variable(name = "vin") String vin) {
        logger.info("VehicleInfoLookup: vin [{}] element [{}]", vin, job.getElementId());

        String vehicleType = VEHICLE_TYPES[random.nextInt(VEHICLE_TYPES.length)];
        String vehicleColor = VEHICLE_COLORS[random.nextInt(VEHICLE_COLORS.length)];
        logger.info("VehicleInfoLookup output - vehicleType: [{}], vehicleColor: [{}]", vehicleType, vehicleColor);

        client.newCompleteCommand(job.getKey())
                .variables("{\"vehicleType\":\"" + vehicleType + "\", \"vehicleColor\":\"" + vehicleColor + "\"}")
                .send()
                .join();
    }

    @JobWorker(type = "quote_provider_api", autoComplete = false)
    public void quoteProviderApi(final JobClient client, final ActivatedJob job) {
        logger.info("QuoteProviderApi: element [{}]", job.getElementId());

        String brand = BRANDS[random.nextInt(BRANDS.length)];
        logger.info("QuoteProviderApi output - brand: [{}]", brand);

        client.newCompleteCommand(job.getKey())
                .variables("{\"brand\":\"" + brand + "\"}")
                .send()
                .join();
    }

    @JobWorker(type = "validate_customer_data", autoComplete = false)
    public void validateCustomerData(final JobClient client, final ActivatedJob job,
                                     @Variable(name = "driversName") String driversName,
                                     @Variable(name = "dob") String dob,
                                     @Variable(name = "vin") String vin,
                                     @Variable(name = "driversLicense") String driversLicense,
                                     @Variable(name = "addressLine") String addressLine,
                                     @Variable(name = "city") String city,
                                     @Variable(name = "state") String state,
                                     @Variable(name = "zipcode") String zipcode) {

        logger.info("Validating customer data for job: {}", job.getElementId());

        boolean completeness = isNonEmpty(driversName)
                && isNonEmpty(dob)
                && isNonEmpty(vin)
                && isNonEmpty(driversLicense)
                && isNonEmpty(addressLine)
                && isNonEmpty(city)
                && isNonEmpty(state)
                && isNonEmpty(zipcode);

        logger.info("Customer data completeness: {}", completeness);

        client.newCompleteCommand(job.getKey())
                .variables("{\"completeness\":" + completeness + "}")
                .send()
                .join();
    }

    private boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
