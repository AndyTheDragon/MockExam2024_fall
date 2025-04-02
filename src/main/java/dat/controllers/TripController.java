package dat.controllers;

import dat.dao.TripDAO;
import dat.dto.TotalPriceDTO;
import dat.dto.TripDTO;
import dat.dto.TripInputDTO;
import dat.enums.TripCategory;
import dat.exceptions.ApiException;
import dat.exceptions.DaoException;
import dat.utils.Populator;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TripController
{
    private final EntityManagerFactory emf;
    private final TripDAO dao;
    private final Logger logger = LoggerFactory.getLogger(TripController.class);

    public TripController(EntityManagerFactory emf)
    {
        this.emf = emf;
        this.dao = new TripDAO(emf);
    }

    public void getAllTrips(Context ctx)
    {
        try
        {
            List<TripDTO> trips = dao.getAll();
            ctx.json(trips);
        } catch (DaoException e)
        {
            throw new ApiException(404, "Trips not found");
        }
    }

    public void getTripById(Context ctx)
    {
        try
        {
            Integer id = ctx.pathParamAsClass("id", Integer.class)
                    .check(i -> i > 0, "ID must be a positive integer")
                    .getOrThrow((validator) -> new IllegalArgumentException("ID must be a positive integer"));
            TripDTO trip = dao.getById(id);
            ctx.json(trip);
        } catch (IllegalArgumentException e)
        {
            throw new ApiException(400, "Invalid ID format", e);
        } catch (DaoException e)
        {
            throw new ApiException(404, "Trip not found", e);
        }
    }

    public void createTrip(Context ctx)
    {
        try
        {
            TripInputDTO tripInput = ctx.bodyAsClass(TripInputDTO.class);
            dao.create(tripInput);
        } catch (IllegalArgumentException | DaoException e)
        {
            throw new ApiException(400, "Invalid input data", e);
        }
    }

    public void updateTrip(Context ctx)
    {
        try
        {
            TripInputDTO tripInput = ctx.bodyAsClass(TripInputDTO.class);
            Integer id = ctx.pathParamAsClass("id", Integer.class)
                    .check(i -> i > 0, "ID must be a positive integer")
                    .getOrThrow((validator) -> new IllegalArgumentException("ID must be a positive integer"));
            TripDTO updatedTrip = dao.update(tripInput, id);
            ctx.json(updatedTrip);
        } catch (IllegalArgumentException | DaoException e)
        {
            throw new ApiException(400, "Invalid input data", e);
        }
    }

    public void deleteTrip(Context ctx)
    {
        try
        {
            Integer id = ctx.pathParamAsClass("id", Integer.class)
                    .check(i -> i > 0, "ID must be a positive integer")
                    .getOrThrow((validator) -> new IllegalArgumentException("ID must be a positive integer"));
            dao.deleteTrip(id);
            ctx.status(204);
        } catch (IllegalArgumentException | DaoException e)
        {
            throw new ApiException(400, "Invalid ID format", e);
        }
    }

    public void addGuideToTrip(Context ctx)
    {
        try
        {
            Integer tripId = ctx.pathParamAsClass("tripId", Integer.class)
                    .check(i -> i > 0, "ID must be a positive integer")
                    .getOrThrow((validator) -> new IllegalArgumentException("Trip ID must be a positive integer"));
            Integer guideId = ctx.pathParamAsClass("guideId", Integer.class)
                    .check(i -> i > 0, "ID must be a positive integer")
                    .getOrThrow((validator) -> new IllegalArgumentException("Guide ID must be a positive integer"));
            dao.addGuideToTrip(tripId, guideId);
            ctx.status(204);
        } catch (IllegalArgumentException | DaoException e)
        {
            throw new ApiException(400, "Invalid input data", e);
        }
    }

    public void populate(Context ctx)
    {
        Populator populator = new Populator();
        populator.populate(emf);
        ctx.status(204);
    }

    public void getByCategory(Context ctx)
    {
        try
        {
            String category = ctx.queryParamAsClass("category", String.class)
                    .check(this::isValidTripCategory, "Invalid category")
                    .getOrThrow((validator) -> new IllegalArgumentException("Category is missing or invalid"));
            List<TripDTO> trips = dao.getByCategory(category);
            ctx.json(trips);
        } catch (DaoException e)
        {
            throw new ApiException(404, "No trips found");
        }
    }

    public void getGuidesTotalPrice(Context ctx)
    {
        List<TotalPriceDTO> totalPriceList = dao.getGuidesTotalPrice();
        ctx.json(totalPriceList);
        throw new ApiException(501, "Not implemented yet");
    }

    private boolean isValidTripCategory(String category)
    {
        try
        {
            TripCategory.valueOf(category.toUpperCase());
            return true;
        } catch (IllegalArgumentException e)
        {
            return false;
        }
    }
}
