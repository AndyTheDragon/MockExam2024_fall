package dat.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.controllers.SecurityController;
import dat.controllers.TripController;
import dat.enums.Roles;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes
{
    private final TripController tripController;
    private final SecurityController securityController;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public Routes(TripController tripController, SecurityController securityController)
    {
        this.tripController = tripController;
        this.securityController = securityController;
    }

    public  EndpointGroup getRoutes()
    {
        return () -> {
            path("trips", tripRoutes());
            path("auth", authRoutes());
        };
    }

    private  EndpointGroup tripRoutes()
    {
        return () -> {
            get(tripController::getAllTrips);
            get("/{id}", tripController::getTripById);
            post(tripController::createTrip);
            put("/{id}", tripController::updateTrip);
            delete("/{id}", tripController::deleteTrip);
            put("/{tripId}/guides/{guideId}", tripController::addGuideToTrip);
            post("/populate", tripController::populate);
        };
    }

    private  EndpointGroup authRoutes()
    {
        return () -> {
            get("/test", ctx->ctx.json(jsonMapper.createObjectNode().put("msg",  "Hello from Open")), Roles.ANYONE);
            get("/healthcheck", securityController::healthCheck, Roles.ANYONE);
            post("/login", securityController::login, Roles.ANYONE);
            post("/register", securityController::register, Roles.ANYONE);
            get("/verify", securityController::verify , Roles.ANYONE);
            get("/tokenlifespan", securityController::timeToLive , Roles.ANYONE);
        };
    }

}
