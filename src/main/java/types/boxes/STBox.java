package types.boxes;

import jnr.ffi.Memory;
import jnr.ffi.Runtime;
import jnr.ffi.annotations.In;
import org.locationtech.jts.io.ParseException;
import types.TemporalObject;
import types.basic.tpoint.TPoint;
import types.collections.time.Time;
import jnr.ffi.Pointer;
import org.locationtech.jts.geom.Geometry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import types.collections.time.tstzset;
import types.collections.time.tstzspan;
import types.collections.time.tstzspanset;
import functions.*;
import types.temporal.Temporal;
import utils.ConversionUtils;
import functions.functions;
import javax.naming.OperationNotSupportedException;


/**
 * Class for representing a spatio-temporal box. Temporal bounds may be inclusive or exclusive.
 * <pre>
 *     ``STBox`` objects can be created with a single argument of type string as in MobilityDB.
 *         >>> STBox('STBOX ZT(((1.0,2.0,3.0),(4.0,5.0,6.0)),[2001-01-01, 2001-01-02])')
 *</pre>
 * <pre>
 *     Another possibility is to provide the different dimensions with the corresponding parameters:
 *         - ``xmin``, ``xmax``, ``ymin``, ``ymax`` for spatial dimension
 *         - ``zmin``, ``zmax`` for the third spatial dimension
 *         - ``tmin``, ``tmax`` for temporal dimension
 *         - ``tmin_inc``, ``tmax_inc`` to specify if the temporal bounds are inclusive or exclusive
 *         - ``geodetic`` to specify if the spatial dimension is geodetic
 *         - ``srid`` to specify the spatial reference system identifier
 *</pre>
 *     Note that at least the 2D spatial dimension or the temporal dimension must be provided.
 *<p>
 *         >>> STBox(xmin=1.0, xmax=4.0, ymin=2.0, ymax=5.0, tmin=datetime(2001, 1, 1), tmax=datetime(2001, 1, 2))
 *
 * @author ARIJIT SAMAL
 */
public class STBox implements Box {
	private final OffsetDateTime tMin = null;
	private final OffsetDateTime tMax = null;
	private boolean isGeodetic = false;
	private final int srid = 0;
	private Pointer _inner = null;
	private boolean tmin_inc = true;
	private boolean tmax_inc = true;

	public STBox _get_box(TemporalObject other){
		return this._get_box(other,true,false);
	}


	/**
	 * Factory method to create new STBox objects
	 *
	 * @param other a temporal object
	 * @param allow_space_only boolean space dimension
	 * @param allow_time_only boolean time dimension
	 * @return a new {@link STBox} object
	 */
	public STBox _get_box(Object other, boolean allow_space_only, boolean allow_time_only){
		STBox other_box=null;
		if(allow_space_only && other instanceof Geometry){
			other_box = new STBox(functions.geo_stbox(ConversionUtils.geo_to_gserialized((Geometry) other, this.geodetic())));
		} else if (other instanceof TPoint) {
			// Create a JNR-FFI runtime instance
			Runtime runtime = Runtime.getSystemRuntime();
			// Allocate memory for an integer (4 bytes) but do not set a value
			Pointer intPointer = Memory.allocate(Runtime.getRuntime(runtime), 4);
			intPointer.putInt(0,1);
			other_box = new STBox(functions.tgeo_stboxes(((TPoint)other).getPointInner(),intPointer));
		} else if (allow_time_only) {
			switch (other) {
				case STBox st -> other_box = new STBox(st.get_inner());
				case tstzset p -> other_box = new STBox(functions.tstzset_stbox(p.get_inner()));
				case tstzspan ps -> other_box = new STBox(functions.tstzspan_stbox(ps.get_inner()));
				case Temporal t -> other_box = new STBox(functions.tstzset_stbox(functions.temporal_tstzspan(t.getInner())));
				case tstzspanset ts -> other_box = new STBox(functions.tstzspanset_stbox(ts.get_inner()));
				default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
			}
		}
		return other_box;
	}

    /* ------------------------- Constructors ---------------------------------- */


	/**
	 * The default constructor
	 */
	public STBox() {

	}
	
	public STBox(Pointer inner){
		this(inner, true, true, false);
	}
	
	public STBox(Pointer inner, boolean tmin_inc, boolean tmax_inc, boolean geodetic){
		this._inner = inner;
		this.tmin_inc = tmin_inc;
		this.tmax_inc = tmax_inc;
		this.isGeodetic = geodetic;
	}

	/**
	 * The string constructor
	 *
	 * @param value - STBox value
	 */

	public STBox(final String value){
		this._inner = functions.stbox_in(value);
	}

	/**
	 * Constructor with x,y,z coordinates and {@link LocalDateTime} values.
	 * @param xmin x minimum float value
	 * @param xmax x maximum float value
	 * @param ymin y minimum float value
	 * @param ymax y maximum float value
	 * @param zmin z minimum float value
	 * @param zmax z maximum float value
	 * @param tmin LocalDateTime minimum value
	 * @param tmax LocalDateTime maximum value
	 * @param tmin_inc tmin boolean inclusion
	 * @param tmax_inc tmax boolean inclusion
	 * @param geodetic boolean geodetic
	 */
	public STBox(float xmin, float xmax, float ymin, float ymax, float zmin, float zmax, LocalDateTime tmin, LocalDateTime tmax, boolean tmin_inc, boolean tmax_inc, boolean geodetic){
		Pointer tstzspan=null;
		boolean hast = tmin != null && tmax != null;
		boolean hasx = !Float.isNaN(xmin) && !Float.isNaN(xmax) && !Float.isNaN(ymin) && !Float.isNaN(ymax);
		boolean hasz = !Float.isNaN(zmin) && !Float.isNaN(zmax);
		if (hast){
			tstzspan = new tstzspan(tmin, tmax, tmin_inc, tmax_inc).get_inner();
		}

		this._inner = functions.stbox_make(hasx, hasz, geodetic, srid, xmin, xmax, ymin, ymax, zmin, zmax, tstzspan);
	}


	/**
	 * Constructor without the z coordinate
	 * @param xmin y minimum float value
	 * @param xmax y maximum float value
	 * @param ymin z minimum float value
	 * @param ymax z maximum float value
	 * @param tmin LocalDateTime minimum value
	 * @param tmax LocalDateTime maximum value
	 */
	public STBox(float xmin, float xmax, float ymin, float ymax, LocalDateTime tmin, LocalDateTime tmax){
		this(xmin,xmax,ymin,ymax,0.0f,0.0f,tmin,tmax,true,true,false);
	}


    /**
     * Constructor without the LocalDateTime aspect (equivalent to a TBox)
     * @param xmin
     * @param xmax
     * @param ymin
     * @param ymax
     * @param zmin
     * @param zmax
     */
	public STBox(float xmin, float xmax, float ymin, float ymax, float zmin, float zmax){
		this(xmin,xmax,ymin,ymax,zmin,zmax,null,null,true,true,false);
	}

	/**
	 * Constructor only with x coordinates and temporal dimension
	 * @param xmin
	 * @param xmax
	 * @param tmin
	 * @param tmax
	 */
	public STBox(float xmin, float xmax, LocalDateTime tmin, LocalDateTime tmax){
		this(xmin,xmax,0.0f,0.0f,0.0f,0.0f,tmin,tmax,true,true,false);
	}




	/**
	 * Returns a copy of "this".
	 *<p>
	 *         MEOS Functions:
	 *             <li>stbox_copy</li>
	 * @return a STBox instance
	 */
	public STBox copy() {
		return new STBox(functions.stbox_copy(this._inner));
	}

	/**
	 * Returns a "STBox" from its WKB representation in hex-encoded ASCII.
	 *<p>
	 *         MEOS Functions:
	 *             <li>stbox_from_hexwkb</li>
	 * @param hexwkb WKB representation in hex-encoded ASCII
	 * @return a new STBox instance
	 */
	public static STBox from_hexwkb(String hexwkb) {
		Pointer result = functions.stbox_from_hexwkb(hexwkb);
		return new STBox(result);
	}


	/**
	 * Returns a "STBox" from a "Geometry".
	 * <p>
	 *         MEOS Functions:
	 *             <li>gserialized_in, geo_to_stbox </li>
	 *
	 * @param geom A `Geometry` instance.
	 * @param geodetic Whether to create a geodetic or geometric `STBox`.
	 * @return a new STBox instance
	 */
	public static STBox from_geometry(Geometry geom, boolean geodetic) {
		return new STBox(functions.geo_stbox(ConversionUtils.geo_to_gserialized(geom,geodetic)));
	}

	public static STBox from_geometry(Geometry geom) {
		boolean geodetic = false;
		return new STBox(functions.geo_stbox(ConversionUtils.geo_to_gserialized(geom,geodetic)));
	}


	/**
	 * Returns a "STBox" from a "Time" instance.
	 *<p>
	 *         MEOS Functions:
	 *         <ul>
	 *             <li>timestamp_to_stbox</li>
	 *             <li>tstzspanset_to_stbox</li>
	 *             <li>tstzset_to_stbox</li>
	 *             <li>tstzspan_to_stbox</li>
	 *         </ul>
	 * @param other a Time instance
	 * @return a new STBox instance
	 */
	public static STBox from_time(Time other) {
		STBox returnValue;
		switch (other){
			case tstzset p -> returnValue = new STBox(functions.tstzset_stbox(p.get_inner()));
			case tstzspan ps -> returnValue = new STBox(functions.tstzspan_stbox(ps.get_inner()));
			case tstzspanset ts -> returnValue = new STBox(functions.tstzspanset_stbox(ts.get_inner()));
			default -> throw new TypeNotPresentException(other.getClass().toString(), new Throwable("Operation not supported with this type"));
		}
		return returnValue;
	}
	
	/*
	public STBox from_expanding_bounding_box_geom(Geometry value, float expansion) {
		Pointer gs = functions.gserialized_in(value.toString(), -1);
		Pointer result = functions.geo_expand_spatial(gs, expansion);
		return new STBox(result);
	}

	 */


    /* Modify Tpoint type
    public STBox from_expanding_bounding_box_tpoint(TPoint value, float expansion){
        Pointer result = tpoint_expand_spatial(value._inner, expansion);
        return new STBox(result);
    }

     */

    /*
    //Modify the from geometry datetime function
    public STBox from_space_datetime(Geometry value, Pointer time){
        return from_geometry_datetime(value,time);
    }

    public STBox from_space_tstzset(Geometry value, Pointer time ){
        return from_geometry_tstzset(value,time);
    }

     */


	/**
	 * Returns a "STBox" from a space and time dimension.
	 *
	 * <p>
	 *
	 *         MEOS Functions:
	 *             <li>geo_timestamp_to_stbox</li>
	 *             <li>geo_tstzset_to_stbox</li>
	 * @param geometry A {@link Geometry} instance representing the space dimension.
	 * @param datetime A `{@link Time} instance representing the time dimension.
	 * @param geodetic Whether to create a geodetic or geometric "STBox".
	 * @return A new {@link STBox} instance.
	 */
	public static STBox from_geometry_datetime(Geometry geometry, LocalDateTime datetime, boolean geodetic){
        Pointer gs = ConversionUtils.geo_to_gserialized(geometry,geodetic);
        Pointer result = functions.geo_timestamptz_to_stbox(gs,ConversionUtils.datetimeToTimestampTz(datetime));
        return new STBox(result);
    }


	/**
	 * Returns a "STBox" from a space and time dimension.
	 *
	 * <p>
	 *
	 *         MEOS Functions:
	 *             <li>geo_timestamp_to_stbox</li>
	 *             <li>geo_tstzset_to_stbox</li>
	 * @param geometry A {@link Geometry} instance representing the space dimension.
	 * @param tstzset A `{@link tstzset} instance representing the time dimension.
	 * @param geodetic Whether to create a geodetic or geometric "STBox".
	 * @return A new {@link STBox} instance.
	 */
    public static STBox from_geometry_tstzspan(Geometry geometry, tstzset tstzset, boolean geodetic){
		Pointer gs = ConversionUtils.geo_to_gserialized(geometry,geodetic);
        Pointer result = functions.geo_tstzspan_to_stbox(gs,tstzset.get_inner());
        return new STBox(result);
    }


	/**
	 * Returns the bounding box of a "TPoint" instance as an "STBox".
	 *
	 * <p>.
	 *
	 *         MEOS Functions:
	 *             <li>tpoint_to_stbox</li>
	 * @param temporal A {@link TPoint} instance.
	 * @return A new {@link STBox} instance.
	 */
    public static STBox from_tpoint(TPoint temporal){
		Runtime runtime = Runtime.getSystemRuntime();
		// Allocate memory for an integer (4 bytes) but do not set a value
		Pointer intPointer = Memory.allocate(Runtime.getRuntime(runtime), 4);
		intPointer.putInt(0,1);
        return new STBox(functions.tgeo_stboxes(temporal.getPointInner(),intPointer));
    }


    /* ------------------------- Output ---------------------------------------- */

	/**
	 *  Returns a string representation of "this".
	 *<p>
	 *         MEOS Functions:
	 *             <li>stbox_out</li>
	 * @param max_decimals number of decimals
	 * @return a String instance
	 */
	public String toString(int max_decimals){
		return functions.stbox_out(this._inner,max_decimals);
	}



    /* ------------------------- Conversions ---------------------------------- */

	/**
	 * Returns the temporal dimension of "this" as a "tstzset" instance.
	 * <p>
	 * MEOS Functions:
	 * <li>stbox_to_tstzset</li>
	 *
	 * @return a new tstzset instance
	 */
    public tstzset to_tstzspan() {
        Pointer result = functions.stbox_tstzspan(this._inner);
        return new tstzset(result);
    }


	/**
	 * Returns the spatial dimension of "this" as a {@link Geometry} instance.
	 *
	 * <p>
	 *
	 *         MEOS Functions:
	 *             <li>stbox_to_geo</li>
	 * @param precision The precision of the geometry coordinates.
	 * @return A new {@link Geometry} instance.
	 * @throws ParseException
	 */
	public Geometry to_geometry(int precision) throws ParseException {
		return ConversionUtils.gserialized_to_shapely_geometry(functions.stbox_geo(this._inner),precision);
	}


    /* ------------------------- Accessors ------------------------------------- */

	/**
	 * Returns whether "this" has a spatial (XY) dimension.
	 *<p>
	 *         MEOS Functions:
	 *             <li>stbox_hasx</li>
	 * @return True if "this" has a spatial dimension, False otherwise.
	 */
	public boolean has_xy() {
		return functions.stbox_hasx(this._inner);
	}


	/**
	 * Returns whether "this" has a Z dimension.
	 *<p>
	 *         MEOS Functions:
	 *             <li>stbox_hasz</li>
	 * @return True if "this" has a Z dimension, False otherwise.
	 */
	public boolean has_z() {
		return functions.stbox_hasz(this._inner);
	}


	/**
	 * Returns whether "this" has a time dimension.
	 *<p>
	 *         MEOS Functions:
	 *             <li>stbox_hast</li>
	 * @return True if "this" has a time dimension, False otherwise.
	 */
	public boolean has_t() {
		return functions.stbox_hast(this._inner);
	}

	/**
	 * Returns whether "this" is geodetic.
	 *<p>
	 *         MEOS Functions:
	 *             <li> stbox_isgeodetic </li>
	 * @return True if "this" is geodetic, False otherwise.
	 */
	public boolean geodetic() {
		return functions.stbox_isgeodetic(this._inner);
	}


	/**
	 * Returns the minimum X coordinate of "this".
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_xmin</li>
	 * @return A {@link Float} with the minimum X coordinate of "this".
	 */
    public float xmin(){
		return (float) functions.stbox_xmin(this._inner).getDouble(0);
    }

	/**
	 * Returns the minimum Y coordinate of "this".
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_ymin</li>
	 * @return A {@link Float} with the minimum Y coordinate of "this".
	 */
    public float ymin(){
		return (float) functions.stbox_ymin(this._inner).getDouble(0);
    }

	/**
	 * Returns the minimum Z coordinate of "this".
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_zmin</li>
	 * @return A {@link Float} with the minimum Z coordinate of "this".
	 */
    public float zmin(){
		return (float) functions.stbox_zmin(this._inner).getDouble(0);
    }

	/**
	 * Returns the minimum T coordinate of "this".
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_tmin</li>
	 * @return A {@link Float} with the minimum T coordinate of "this".
	 */
    public float tmin(){
		return (float) functions.stbox_tmin(this._inner).getDouble(0);
    }

	/**
	 * Returns the maximum X coordinate of "this".
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_xmax</li>
	 * @return A {@link Float} with the maximum X coordinate of "this".
	 */
    public float xmax(){
		return (float) functions.stbox_xmax(this._inner).getDouble(0);
    }

	/**
	 * Returns the maximum Y coordinate of "this".
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_ymax</li>
	 * @return A {@link Float} with the maximum Y coordinate of "this".
	 */
    public float ymax(){
		return (float) functions.stbox_ymax(this._inner).getDouble(0);
    }

	/**
	 * Returns the maximum Z coordinate of "this".
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_zmax</li>
	 * @return A {@link Float} with the maximum Z coordinate of "this".
	 */
    public float zmax(){
		return (float) functions.stbox_zmax(this._inner).getDouble(0);
    }

	/**
	 * Returns the maximum T coordinate of "this".
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_tmax</li>
	 * @return A {@link Float} with the maximum T coordinate of "this".
	 */
    public float tmax(){
		return (float) functions.stbox_tmax(this._inner).getDouble(0);
    }

	public boolean get_tmin_inc(){
		return tmin_inc;
	}

	public boolean get_tmax_inc(){
		return tmax_inc;
	}

	public OffsetDateTime getTMin() {
		return tMin;
	}

	public OffsetDateTime getTMax() {
		return tMax;
	}

	public boolean isGeodetic() {
		return isGeodetic;
	}

	public int getSrid() {
		return srid;
	}

	public Pointer get_inner(){
		return this._inner;
	}


    /* ------------------------- Spatial Reference System ---------------------- */

	/**
	 * Returns the SRID of "this".
	 *<p>
	 *         MEOS Functions:
	 *             <li>stbox_srid</li>
	 * @return an Integer with the SRID of "this"
	 */
	public int srid(){
		return functions.stbox_srid(this._inner);
	}


	/**
	 * Returns a copy of "this" with the SRID set to "value".
	 *<p>
	 *         MEOS Functions:
	 *             <li>stbox_set_srid</li>
	 * @param value The new SRID.
	 * @return a new STBox instance
	 */
	public STBox set_srid(int value) {
		return new STBox(functions.stbox_set_srid(this._inner,value));
	}
	

	/* ------------------------- Transformations ------------------------------- */

	/**
	 * Get the spatial dimension of "this", removing the temporal dimension
	 *         if any
	 *
	 * <p>
	 *
	 *         MEOS Functions:
	 *             <li>stbox_get_space</li>
	 * @return A new {@link STBox} instance.
	 */
	public STBox get_space(){
		return new STBox(functions.stbox_get_space(this._inner));
	}


	/**
	 * Expands "this" with "other".
	 *         If "other" is a {@link Integer} or a {@link Float}, the result is equal
	 *         to "this" but with the spatial dimensions expanded by "other" in all
	 *         directions. If "other" is a {@link java.time.Duration}, the result is equal to
	 *         "this"  but with the temporal dimension expanded by `other` in both
	 *         directions.
	 *
	 * <p>
	 *
	 *         MEOS Functions:
	 *             <li>stbox_expand_space</li>
	 *             <li>stbox_expand_time</li>
	 * @param stbox The object to expand "this" with.
	 * @param other The object to expand "this" with.
	 * @return A new {@link STBox} instance.
	 */
	public STBox expand_stbox(STBox stbox, STBox other) {
		Pointer result = functions.stbox_copy(this._inner);
//		functions.stbox_expand_space(other._inner, result);
		return new STBox(result);
	}


	/**
	 * Expands "this" with "other".
	 *         If "other" is a {@link Integer} or a {@link Float}, the result is equal
	 *         to "this" but with the spatial dimensions expanded by "other" in all
	 *         directions. If "other" is a {@link java.time.Duration}, the result is equal to
	 *         "this"  but with the temporal dimension expanded by `other` in both
	 *         directions.
	 *
	 * <p>
	 *
	 *         MEOS Functions:
	 *             <li>stbox_expand_space</li>
	 *             <li>stbox_expand_time</li>
	 * @param value The value to expand with
	 * @return A new {@link STBox} instance.
	 */
	public STBox expand_numerical(Number value) {
		STBox result = null;
		if(value instanceof Integer || value instanceof Float){
			result = new STBox(functions.stbox_expand_space(this.get_inner(), (double) value.floatValue()));
		}
		return result;
	}


	/**
	 * Returns "this" rounded to the given number of decimal digits.
	 *<p>
	 *         MEOS Functions:
	 *            <li>stbox_round </li>
	 *
	 * @param maxdd Maximum number of decimal digits.
	 * @return a new STBox instance
	 */
	public STBox round(int maxdd) {
		Pointer new_inner = functions.stbox_copy(this._inner);
		functions.stbox_round(new_inner,maxdd);
		return new STBox(new_inner);
	}






    /* ------------------------- Set Operations -------------------------------- */


	/**
	 * Returns the union of "this" with "other". Fails if the union is not contiguous.
	 *<p>
	 *         MEOS Functions:
	 *             <li>union_stbox_stbox</li>
	 * @param other spatiotemporal box to merge with
	 * @param strict included or not
	 * @return a new STBox instance
	 */
	public STBox union(STBox other, boolean strict) {
		return new STBox(functions.union_stbox_stbox(this._inner, other._inner, strict));
	}


	/**
	 * Returns the union of "this" with "other". Fails if the union is not contiguous.
	 *<p>
	 *         MEOS Functions:
	 *             <li>union_stbox_stbox</li>
	 * @param other spatiotemporal box to merge with
	 * @return a new STBox instance
	 */
	public STBox add(STBox other) {
		return this.union(other, true);
	}


	/**
	 * Returns the intersection of "this" with "other".
	 *<p>
	 *         MEOS Functions:
	 *             <li>intersection_stbox_stbox </li>
	 *
	 * @param other temporal object to merge with
	 * @return a new STBox instance if the instersection is not empty, `None` otherwise.
	 */
	public STBox intersection(STBox other) {
		return new STBox(functions.intersection_stbox_stbox(this._inner,other.get_inner()));
	}


	/**
	 * Returns the intersection of "this" with "other".
	 *<p>
	 *         MEOS Functions:
	 *             <li>intersection_stbox_stbox </li>
	 *
	 * @param other temporal object to merge with
	 * @return a new STBox instance if the instersection is not empty, `None` otherwise.
	 */
	public STBox mul(STBox other) {
		return this.intersection(other);
	}




	/* ------------------------- Positions Operators ------------------------------------- */



	/**
	 * Returns whether ``self`` and `other` are adjacent. Two spatiotemporal boxes are adjacent if they share n
	 *         dimensions and the intersection is of at most n-1 dimensions. Note that for `TPoint` instances, the bounding box
	 *         of the temporal point is used.
	 *<p></p>
	 *<p>
	 *         MEOS Functions:
	 *             <li>adjacent_stbox_stbox</li>
	 * @param other The other spatiotemporal object to check adjacency with "this".
	 * @return "true" if "this" and "other" are adjacent, "false" otherwise.
	 */
	public boolean is_adjacent(TemporalObject other) {
		return functions.adjacent_stbox_stbox(this._inner,this._get_box(other,true,true).get_inner());
	}

	/**
	 * Returns whether "this" is contained in "other". Note that for "TPoint" instances, the bounding
	 *         box of the temporal point is used.
	 *<p>
	 *         MEOS Functions:
	 *             <li>contained_stbox_stbox</li>
	 * @param other The spatiotemporal object to check containment with "this.
	 * @return "true" if "this" is contained in "other", "false" otherwise.
	 */
	public boolean is_contained_in(TemporalObject other) {
		return functions.contained_stbox_stbox(this._inner,this._get_box(other,true,true).get_inner());
	}

	/**
	 * Returns whether "this" contains `content`. Note that for "TPoint" instances, the bounding box of
	 *         the temporal point is used.
	 *
	 *  <p>
	 *
	 *         MEOS Functions:
	 *             <li>contains_stbox_stbox </li>
	 * @param other The spatiotemporal object to check containment with "this".
	 * @return "true" if "this" contains "other", "false otherwise.
	 */
	public boolean contains(TemporalObject other) {
		return functions.contains_stbox_stbox(this._inner,this._get_box(other,true,true).get_inner());
	}

	/**
	 * Returns whether "this" overlaps "other". Note that for "TPoint" instances, the bounding box of
	 *         the temporal point is used.
	 *<p>
	 *         MEOS Functions:
	 *             <li>overlaps_stbox_stbox </li>
	 * @param other The spatiotemporal object to check overlap with "this".
	 * @return "true" if "this" overlaps "other", "false" otherwise.
	 */
	public boolean overlaps(TemporalObject other)  {
		return functions.overlaps_stbox_stbox(this._inner,this._get_box(other,true,true).get_inner());
	}

	/**
	 * Returns whether "this" is the same as "other". Note that for "TPoint" instances, the bounding box of
	 *         the temporal point is used.
	 *<p>
	 *         MEOS Functions:
	 *             <li>same_stbox_stbox</li>
	 * @param other The spatiotemporal object to check equality with "this".
	 * @return "true" if "this" is the same as "other", "false" otherwise.
	 */
	public boolean is_same(TemporalObject other) {
		return functions.same_stbox_stbox(this._inner,this._get_box(other,true,true).get_inner());
	}

	/**
	 * Returns whether "this" is strictly to the left  of "other". Checks the X dimension.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>left_stbox_stbox </li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is strictly to the left of "other", "false" otherwise.
	 */
	public boolean is_left(TemporalObject other) {
		return functions.left_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}

	/**
	 * Returns whether "this" is to the left "other" allowing for overlap. That is, "this" does not extend
	 *         to the right of "other. Checks the X dimension.
	 *<p>
	 *         MEOS Functions:
	 *             <li>overleft_stbox_stbox, tpoint_to_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is to the left of "other", "false" otherwise.
	 */
	public boolean is_over_or_left(TemporalObject other) {
		return functions.overleft_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}

	/**
	 * Returns whether "this" is strictly to the right of "other". Checks the X dimension.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>right_stbox_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is strictly to the right of "other", "false" otherwise.
	 */
	public boolean is_right(TemporalObject other) {
		return functions.right_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}

	/**
	 * Returns whether "this" is to the right of "other" allowing for overlap. That is, "this" does not
	 *         extend to the left of "other". Checks the X dimension.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>overright_stbox_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is to the right of "other", "false" otherwise.
	 */
	public boolean is_over_or_right(TemporalObject other) {
		return functions.overright_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}

	/**
	 * Returns whether "this" is strictly below "other". Checks the Y dimension.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>below_stbox_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is strictly below of "other", "false" otherwise.
	 */
	public boolean is_below(TemporalObject other) {
		return functions.below_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}


	/**
	 * Returns whether "this" is below "other" allowing for overlap. That is, "this" does not extend
	 *         above "other". Checks the Y dimension.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>overbelow_stbox_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is below "other" allowing for overlap, "false" otherwise.
	 */
	public boolean is_over_or_below(TemporalObject other) {
		return functions.overbelow_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}


	/**
	 * Returns whether "this" is strictly above "other". Checks the Y dimension.
	 *
	 *<p>
	 *         MEOS Functions:
	 *             <li>above_stbox_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is strictly above of "other", "false" otherwise.
	 */
	public boolean is_above(TemporalObject other) {
		return functions.above_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}


	/**
	 * Returns whether "this" is above "other" allowing for overlap. That is, "this" does not extend
	 *         below "other". Checks the Y dimension.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>overabove_stbox_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is above "other" allowing for overlap, "false" otherwise.
	 */
	public boolean is_over_or_above(TemporalObject other) {
		return functions.overabove_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}


	/**
	 * Returns whether "this" is strictly in front of "other". Checks the Z dimension.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>front_stbox_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is strictly in front of "other", "false" otherwise.
	 */
	public boolean is_front(TemporalObject other) {
		return functions.front_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}


	/**
	 * Returns whether "this" is in front of "other" allowing for overlap. That is, "this" does not extend
	 *         behind "other". Checks the Z dimension.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>overfront_stbox_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is in front of "other" allowing for overlap, "false" otherwise.
	 */
	public boolean is_over_or_front(TemporalObject other) {
		return functions.overfront_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}


	/**
	 * Returns whether "this" is strictly behind "other". Checks the Z dimension.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>back_stbox_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is strictly behind of "other", "false" otherwise.
	 */
	public boolean is_behind(TemporalObject other) {
		return functions.back_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}


	/**
	 * Returns whether "this" is behind "other" allowing for overlap. That is, "this" does not extend
	 *         in front of "other". Checks the Z dimension.
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>overback_stbox_stbox</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is behind of "other" allowing for overlap, "false" otherwise.
	 */
	public boolean is_over_or_behind(TemporalObject other) {
		return functions.overback_stbox_stbox(this._inner,this._get_box(other).get_inner());
	}


	/**
	 *  Returns whether "this" is strictly before "other". Checks the time dimension.
	 *
	 * <p>
	 *         See Also:
	 * 				{@link tstzset#is_before(TemporalObject)}
	 * 	<p>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is strictly before "other", "false" otherwise.
	 */
	public boolean is_before(TemporalObject other) throws Exception {
		return this.to_tstzspan().is_before(other);
	}


	/**
	 *  Returns whether "this" is before "other" allowing for overlap. That is, "this" does not extend
	 *         after "other". Checks the time dimension.
	 *
	 * <p>
	 *     See Also:
	 * 	 			{@link tstzset#is_over_or_before(TemporalObject)}
	 * 	 </p>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is before "other" allowing for overlap, "false" otherwise.
	 */
	public boolean is_over_or_before(TemporalObject other) throws Exception {
		return this.to_tstzspan().is_over_or_before(other);
	}


	/**
	 * Returns whether "this" is strictly after "other". Checks the time dimension.
	 *
	 * <p>
	 *      See Also:
	 * 	 			{@link tstzset#is_after(TemporalObject)}
	 *     </p>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is strictly after "other", "false" otherwise.
	 */
	public boolean is_after(TemporalObject other) throws Exception {
		return this.to_tstzspan().is_after(other);
	}


	/**
	 * Returns whether "this" is after "other" allowing for overlap. That is, "this does not extend
	 *         before "other". Checks the time dimension.
	 *
	 *  <p>
	 *      See Also:
	 * 				{@link tstzset#is_over_or_after(TemporalObject)}
	 *      </p>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is after "other" allowing for overlap, "false" otherwise.
	 */
	public boolean is_over_or_after(TemporalObject other) throws Exception {
		return this.to_tstzspan().is_over_or_after(other);
	}

    /* ------------------------- Distance Operations --------------------------- */


	/**
	 * Returns the distance between the nearest points of "this" and "other".
	 *
	 * <p>
	 *
	 *         MEOS Functions:
	 *         <ul>
	 *             <li>nad_stbox_geo</li>
	 *             <li>nad_stbox_stbox</li>
	 *          </ul>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return a Float instance with the distance between the nearest points of "this" and "``other``".
	 */
	public float nearest_approach_distance_geom(Geometry other) {
		return (float) functions.nad_stbox_geo(this._inner, ConversionUtils.geo_to_gserialized(other, this.geodetic()));
	}


	/**
	 * Returns the distance between the nearest points of "this" and "other".
	 *
	 * <p>
	 *
	 *         MEOS Functions:
	 *         <ul>
	 *             <li>nad_stbox_geo</li>
	 *             <li>nad_stbox_stbox</li>
	 *          </ul>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return a Float instance with the distance between the nearest points of "this" and "``other``".
	 */
	public float nearest_approach_distance_stbox(STBox other) {
		return (float) functions.nad_stbox_stbox(this._inner, other._inner);
	}


	/**
	 * Returns the distance between the nearest points of "this" and "other".
	 *
	 * <p>
	 *
	 *         MEOS Functions:
	 *         <ul>
	 *             <li>nad_stbox_geo</li>
	 *             <li>nad_stbox_stbox</li>
	 *          </ul>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return a Float instance with the distance between the nearest points of "this" and "``other``".
	 */
	public float nearest_approach_distance_tpoint(TPoint other) {
		return (float) functions.nad_tgeo_stbox(this._inner, other.getPointInner());
	}



    /* ------------------------- Comparisons ----------------------------------- */

	/**
	 * Returns whether "this" is equal to "other".
	 *
	 *  <p>
	 *         MEOS Functions:
	 *             <li>stbox_eq</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is equal to "other", "false" otherwise.
	 */
	public boolean eq(Box other) {
		boolean result;
		result = other instanceof STBox && functions.stbox_eq(this._inner, ((STBox) other).get_inner());
		return result;
	}


	/**
	 * Returns whether "this" is not equal to "other".
	 *
	 *  <p>
	 *         MEOS Functions:
	 *             <li>stbox_ne</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is not equal to "other", "false" otherwise.
	 */
	public boolean notEquals(Box other) {
		boolean result;
		result = !(other instanceof STBox) || functions.stbox_ne(this._inner, ((STBox) other).get_inner());
		return result;
	}

	/**
	 * Returns whether "this" is less than "other". Compares first the SRID, then the time dimension,
	 *         and finally the spatial dimension (X, then Y then Z lower bounds and then the upper bounds).
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_lt</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is less than "other", "false" otherwise.
	 */
	public boolean lessThan(Box other) throws OperationNotSupportedException {
		if (other instanceof STBox){
			return functions.stbox_lt(this._inner,((STBox) other).get_inner());
		}
		else{
			throw new OperationNotSupportedException("Operation not supported with this type.");
		}
	}


	/**
	 * Returns whether "this" is less than or equal to "other". Compares first the SRID, then the time dimension,
	 *         and finally the spatial dimension (X, then Y then Z lower bounds and then the upper bounds).
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_le</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is less than or equal "other", "false" otherwise.
	 */
	public boolean lessThanOrEqual(Box other) throws OperationNotSupportedException {
		if (other instanceof STBox){
			return functions.stbox_le(this._inner,((STBox) other).get_inner());
		}
		else{
			throw new OperationNotSupportedException("Operation not supported with this type.");
		}
	}


	/**
	 * Returns whether "this" is greater than "other". Compares first the SRID, then the time dimension,
	 *         and finally the spatial dimension (X, then Y then Z lower bounds and then the upper bounds).
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_gt</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is greater "other", "false" otherwise.
	 */
	public boolean greaterThan(Box other) throws OperationNotSupportedException {
		if (other instanceof STBox){
			return functions.stbox_gt(this._inner,((STBox) other).get_inner());
		}
		else{
			throw new OperationNotSupportedException("Operation not supported with this type.");
		}
	}

	/**
	 *  Returns whether "this" is greater than or equal to "other". Compares first the SRID, then the time dimension,
	 *         and finally the spatial dimension (X, then Y then Z lower bounds and then the upper bounds).
	 *
	 * <p>
	 *         MEOS Functions:
	 *             <li>stbox_ge</li>
	 * @param other The spatiotemporal object to compare with "this".
	 * @return "true" if "this" is greater or equal "other", "false" otherwise.
	 */
	public boolean greaterThanOrEqual(Box other) throws OperationNotSupportedException {
		if (other instanceof STBox){
			return functions.stbox_ge(this._inner,((STBox) other).get_inner());
		}
		else{
			throw new OperationNotSupportedException("Operation not supported with this type.");
		}
	}

	@Override
	public tstzspan to_period(){
		error_handler_fn errorHandler = new error_handler();
		functions.meos_initialize();
		return new tstzspan(functions.stbox_tstzspan(this._inner));
	}

	/* ------------------------- Splitting ----------------------------------- */
/**
        Returns a list of 4 (or 8 if `self`has Z dimension) :class:`STBox`
        instances resulting from the quad  split of ``self``.
<p>
        Indices of returned array are as follows (back only present if Z
        dimension is present):
<p>
           >>> #    (front)          (back)<br>
           >>> # -------------   -------------<br>
           >>> # |  2  |  3  |   |  6  |  7  |<br>
           >>> # ------------- + -------------<br>
           >>> # |  0  |  1  |   |  4  |  5  |<br>
           >>> # -------------   -------------<br>
<p>
        Returns:<br>
            A :class:`list` of :class:`STBox` instances.<br>
<p>
        MEOS Functions:<br>
            stbox_quad_split
*/
	public List<STBox> quad_split_flat(){
		// Create a JNR-FFI runtime instance
		Runtime runtime = Runtime.getSystemRuntime();
		// Allocate memory for an integer (4 bytes) but do not set a value
		Pointer intPointer = Memory.allocate(Runtime.getRuntime(runtime), 4);
		Pointer resPointer= functions.stbox_quad_split(this.get_inner(), intPointer);
		int count= intPointer.getInt(Integer.BYTES);
		List<STBox> stBoxList= new ArrayList<>();
		for(int i=0;i<count;i++){
			Pointer p= resPointer.getPointer((long) i *Long.BYTES);
			STBox stbox= new STBox(p);
			stBoxList.add(stbox);
		}
		return stBoxList;
	}

	/**
        Returns a 2D (YxX) or 3D (ZxYxX) list of :class:`STBox` instances
        resulting from the quad split of ``self``.<p>

        Indices of returned array are as follows:<p>

           >>> #       (front)<br>
           >>> # -------------------<br>
           >>> # | [1][0] | [1][1] |<br>
           >>> # -------------------<br>
           >>> # | [0][0] | [0][1] |<br>
           >>> # -------------------<br>
	    <p>
        If Z dimension is present:<br>

           >>> #          (front)                      (back)<br>
           >>> # -------------------------   -------------------------<br>
           >>> # | [0][1][0] | [0][1][1] |   | [1][1][0] | [1][1][1] |<br>
           >>> # ------------------------- + -------------------------<br>
           >>> # | [0][0][0] | [0][0][1] |   | [1][0][0] | [1][0][1] |<br>
           >>> # -------------------------   -------------------------<br>
	    <p>

        Returns:<br>
            A 2D or 3D :class:`list` of :class:`STBox` instances.
        <p>
        MEOS Functions:<br>
            stbox_quad_split

    */
	public List<List<List<STBox>>> quad_split() {
		// Create a JNR-FFI runtime instance
		Runtime runtime = Runtime.getSystemRuntime();
		// Allocate memory for an integer (4 bytes) but do not set a value
		Pointer intPointer = Memory.allocate(Runtime.getRuntime(runtime), 4);
		Pointer resPointer= functions.stbox_quad_split(this.get_inner(), intPointer); // Populate boxes and count
		List<STBox> boxes= new ArrayList<>();
		for(int i=0;i<8;i++){
			STBox stBox= new STBox(resPointer.getPointer(i*Long.BYTES));
			boxes.add(stBox);
		}

		if (this.has_z()) {
			// 3D case (ZxYxX)
			List<List<List<STBox>>> result = new ArrayList<>();
			// Front part
			List<List<STBox>> front = new ArrayList<>();
			front.add(List.of(boxes.get(0), boxes.get(1))); // [0][1][0], [0][1][1]
			front.add(List.of(boxes.get(2), boxes.get(3))); // [0][0][0], [0][0][1]

			// Back part
			List<List<STBox>> back = new ArrayList<>();
			back.add(List.of(boxes.get(4), boxes.get(5))); // [1][1][0], [1][1][1]
			back.add(List.of(boxes.get(6), boxes.get(7))); // [1][0][0], [1][0][1]

			result.add(front);
			result.add(back);
			return result;
		} else {
			// 2D case (YxX)
			List<List<STBox>> result = new ArrayList<>();
			result.add(List.of(boxes.get(0), boxes.get(1))); // [1][0], [1][1]
			result.add(List.of(boxes.get(2), boxes.get(3))); // [0][0], [0][1]
			return List.of(result);
		}
	}

	/**
        Returns a list of `STBox` instances representing the tiles of
        ``self``.

        Args:
            size: The size of the spatial tiles. If the `STBox` instance has a
                spatial dimension and this argument is not provided, the tiling
                will be only temporal.
            duration: The duration of the temporal tiles. If the `STBox`
                instance has a time dimension and this argument is not
                provided, the tiling will be only spatial.
            origin: The origin of the spatial tiling. If not provided, the
                origin will be (0, 0, 0).
            start: The start time of the temporal tiling. If not provided,
                the start time used by default is Monday, January 3, 2000.

        Returns:
            A list of `STBox` instances.

        MEOS Functions:
            stbox_tile_list
     */
//	public List<STBox> tile(double size, Object duration, Geometry origin, Object start){
//		double sz = Optional.of(size).orElse(
//                (double) (Math.max(Math.max(xmax() - xmin(), ymax() - ymin()), has_z() ? zmax() - zmin() : 0) + 1)
//        );
//
//		Pointer dt= null;
//		if(duration instanceof Duration){
//			dt= ConversionUtils.timedelta_to_interval((Duration) duration);
//		}
//		else{
//			if(duration instanceof String){
//				dt= functions.pg_interval_in(duration.toString(), -1);
//			}
//			else dt = null;
//		}
//
//		OffsetDateTime st= null;
//		if(start instanceof LocalDateTime){
//			st= ConversionUtils.datetimeToTimestampTz((LocalDateTime) start);
//		}
//		else{
//			if(start instanceof String){
//				st= functions.pg_timestamptz_in(start.toString(), -1);
//			}
//			else{
//				if(this.has_t()){
//					st= functions.pg_timestamptz_in("2000-01-03", -1);
//				}
//				else{
//					st= null;
//				}
//			}
//		}
//
//		Pointer gs= null;
//		if(origin!=null){
//			gs= ConversionUtils.geo_to_gserialized(origin, this.geodetic());
//		}
//		else{
//			if(this.geodetic()){
//				gs= functions.pgis_geography_in("Point(0 0 0)", -1);
//			}
//			else{
//				gs= functions.pgis_geometry_in("Point (0 0 0)", -1);
//			}
//		}
//
//		// Create a JNR-FFI runtime instance
//		Runtime runtime = Runtime.getSystemRuntime();
//		// Allocate memory for an integer (4 bytes) but do not set a value
//		Pointer intPointer = Memory.allocate(Runtime.getRuntime(runtime), 4);
//		Pointer resPointer= functions.stbox_space_time_tiles(this.get_inner(), sz, sz, sz, dt, gs, st);
//		int count= intPointer.getInt(Integer.BYTES);
//		List<STBox> stBoxes= new ArrayList<>();
//		for(int i=0;i<count;i++){
//			Pointer p= resPointer.getPointer((long) i *Long.BYTES);
//			STBox s= new STBox(p);
//			stBoxes.add(s);
//		}
//		return stBoxes;
//	}
}
