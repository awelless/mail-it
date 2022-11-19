package it.mail.distribution

import io.smallrye.health.api.AsyncHealthCheck
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.health.HealthCheckResponse
import org.eclipse.microprofile.health.Liveness

@Liveness
@ApplicationScoped
class LivenessHealthCheck : AsyncHealthCheck {

    override fun call(): Uni<HealthCheckResponse> =
        Uni.createFrom().item(HealthCheckResponse.up("Mail-it is running"))
}
