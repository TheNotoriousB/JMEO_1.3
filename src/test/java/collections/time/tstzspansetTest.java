package collections.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import functions.functions;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import types.TemporalObject;
import types.basic.tfloat.TFloatInst;
import types.basic.tfloat.TFloatSeq;
import types.basic.tfloat.TFloatSeqSet;
import types.boxes.STBox;
import types.boxes.TBox;
import types.collections.time.tstzspan;
import types.collections.time.tstzspanset;
import types.collections.time.tstzset;
import types.collections.time.Time;
import functions.*;
import javax.naming.OperationNotSupportedException;

import static org.junit.jupiter.api.Assertions.*;

class tstzspansetTest {
	tstzspanset pset = new tstzspanset("{[2019-09-01 00:00:00+00, 2019-09-02 00:00:00+00], [2019-09-03 00:00:00+00, 2019-09-04 00:00:00+00]}");
	tstzspanset pset2 = new tstzspanset("{[2019-09-01 00:00:00+00, 2019-09-02 00:00:00+00), (2019-09-02 00:00:00+00, 2019-09-04 00:00:00+00]}");

    tstzspansetTest() throws SQLException {
    }

	static error_handler_fn errorHandler= new error_handler();
	
	private static Stream<Arguments> temporals_adjacent() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspan("(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0)"), false),
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), false),
				Arguments.of(new TFloatInst("1.0@2020-01-01"), false),
				Arguments.of(new TFloatSeq("(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31]"), false),
				Arguments.of(new TFloatSeqSet("{(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31], (1.0@2021-01-01, 3.0@2021-01-10, 10.0@2021-01-20, 0.0@2021-01-31]}"), false),
				Arguments.of(new TBox("TBOXFLOAT XT([0, 10),[2020-01-01, 2020-01-31])"), false),
				Arguments.of(new STBox("STBOX ZT(((1.0,2.0,3.0),(4.0,5.0,6.0)),[2001-01-01, 2001-01-02])"), false)
		);
	}


	private static Stream<Arguments> temporals_iscontained() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspan("(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0)"), false),
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), false),
				Arguments.of(new TFloatInst("1.0@2020-01-01"), false),
				Arguments.of(new TFloatSeq("(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31]"), false),
				Arguments.of(new TFloatSeqSet("{(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31], (1.0@2021-01-01, 3.0@2021-01-10, 10.0@2021-01-20, 0.0@2021-01-31]}"), false),
				Arguments.of(new TBox("TBOXFLOAT XT([0, 10),[2020-01-01, 2020-01-31])"), false),
				Arguments.of(new STBox("STBOX ZT(((1.0,2.0,3.0),(4.0,5.0,6.0)),[2001-01-01, 2001-01-02])"), false)
		);
	}


	private static Stream<Arguments> temporals_contains() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspan("(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0)"), false),
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), false),
				Arguments.of(new TFloatInst("1.0@2020-01-01"), false),
				Arguments.of(new TFloatSeq("(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31]"), false),
				Arguments.of(new TFloatSeqSet("{(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31], (1.0@2021-01-01, 3.0@2021-01-10, 10.0@2021-01-20, 0.0@2021-01-31]}"), false),
				Arguments.of(new TBox("TBOXFLOAT XT([0, 10),[2020-01-01, 2020-01-31])"), false),
				Arguments.of(new STBox("STBOX ZT(((1.0,2.0,3.0),(4.0,5.0,6.0)),[2001-01-01, 2001-01-02])"), false)
		);
	}


	private static Stream<Arguments> temporals_overlaps() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspan("(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0)"), false),
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), false),
				Arguments.of(new TFloatInst("1.0@2020-01-01"), false),
				Arguments.of(new TFloatSeq("(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31]"), false),
				Arguments.of(new TFloatSeqSet("{(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31], (1.0@2021-01-01, 3.0@2021-01-10, 10.0@2021-01-20, 0.0@2021-01-31]}"), false),
				Arguments.of(new TBox("TBOXFLOAT XT([0, 10),[2020-01-01, 2020-01-31])"), false),
				Arguments.of(new STBox("STBOX ZT(((1.0,2.0,3.0),(4.0,5.0,6.0)),[2001-01-01, 2001-01-02])"), false)
		);
	}


	private static Stream<Arguments> temporals_same() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspan("(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0)"), false),
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), false),
				Arguments.of(new TFloatInst("1.0@2020-01-01"), false),
				Arguments.of(new TFloatSeq("(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31]"), false),
				Arguments.of(new TFloatSeqSet("{(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31], (1.0@2021-01-01, 3.0@2021-01-10, 10.0@2021-01-20, 0.0@2021-01-31]}"), false),
				Arguments.of(new TBox("TBOXFLOAT XT([0, 10),[2020-01-01, 2020-01-31])"), false),
				Arguments.of(new STBox("STBOX ZT(((1.0,2.0,3.0),(4.0,5.0,6.0)),[2001-01-01, 2001-01-02])"), false)
		);
	}


	private static Stream<Arguments> temporals_before() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspan("(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0)"), true),
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), true),
				Arguments.of(new TFloatInst("1.0@2020-01-01"), true),
				Arguments.of(new TFloatSeq("(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31]"), true),
				Arguments.of(new TFloatSeqSet("{(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31], (1.0@2021-01-01, 3.0@2021-01-10, 10.0@2021-01-20, 0.0@2021-01-31]}"), true),
				Arguments.of(new TBox("TBOXFLOAT XT([0, 10),[2020-01-01, 2020-01-31])"), true),
				Arguments.of(new STBox("STBOX ZT(((1.0,2.0,3.0),(4.0,5.0,6.0)),[2001-01-01, 2001-01-02])"), false)
		);
	}


	private static Stream<Arguments> temporals_after() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspan("(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0)"), false),
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), false),
				Arguments.of(new TFloatInst("1.0@2020-01-01"), false),
				Arguments.of(new TFloatSeq("(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31]"), false),
				Arguments.of(new TFloatSeqSet("{(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31], (1.0@2021-01-01, 3.0@2021-01-10, 10.0@2021-01-20, 0.0@2021-01-31]}"), false),
				Arguments.of(new TBox("TBOXFLOAT XT([0, 10),[2020-01-01, 2020-01-31])"), false),
				Arguments.of(new STBox("STBOX ZT(((1.0,2.0,3.0),(4.0,5.0,6.0)),[2001-01-01, 2001-01-02])"), true)
		);
	}


	private static Stream<Arguments> temporals_overbefore() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspan("(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0)"), true),
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), true),
				Arguments.of(new TFloatInst("1.0@2020-01-01"), true),
				Arguments.of(new TFloatSeq("(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31]"), true),
				Arguments.of(new TFloatSeqSet("{(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31], (1.0@2021-01-01, 3.0@2021-01-10, 10.0@2021-01-20, 0.0@2021-01-31]}"), true),
				Arguments.of(new TBox("TBOXFLOAT XT([0, 10),[2020-01-01, 2020-01-31])"), true),
				Arguments.of(new STBox("STBOX ZT(((1.0,2.0,3.0),(4.0,5.0,6.0)),[2001-01-01, 2001-01-02])"), false)
		);
	}


	private static Stream<Arguments> temporals_overafter() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspan("(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0)"), false),
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), false),
				Arguments.of(new TFloatInst("1.0@2020-01-01"), false),
				Arguments.of(new TFloatSeq("(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31]"), false),
				Arguments.of(new TFloatSeqSet("{(1.0@2020-01-01, 3.0@2020-01-10, 10.0@2020-01-20, 0.0@2020-01-31], (1.0@2021-01-01, 3.0@2021-01-10, 10.0@2021-01-20, 0.0@2021-01-31]}"), false),
				Arguments.of(new TBox("TBOXFLOAT XT([0, 10),[2020-01-01, 2020-01-31])"), false),
				Arguments.of(new STBox("STBOX ZT(((1.0,2.0,3.0),(4.0,5.0,6.0)),[2001-01-01, 2001-01-02])"), true)
		);
	}


	private static Stream<Arguments> intersection() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspan("(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0)"), true),
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), true)
		);
	}

	private static Stream<Arguments> other() {
		functions.meos_initialize("UTC", errorHandler);
		return Stream.of(
				Arguments.of(new tstzspanset("{(2020-01-01 00:00:00+0, 2020-01-31 00:00:00+0), (2021-01-01 00:00:00+0, 2021-01-31 00:00:00+0)}"), true)
		);
	}




	public void assert_tstzspanset_equality(tstzspanset pset, List<tstzset> plist){
		assertEquals(pset.num_periods(), 2);
	}

	@Test
	public void testStringConstructor(){
		functions.meos_initialize("UTC", errorHandler);
		System.out.println(pset.toString());
		assert_tstzspanset_equality(this.pset,null);
	}

	@Test
	public void testtstzspansetListConstructor(){
		functions.meos_initialize("UTC", errorHandler);
		List<tstzspan> lst = new ArrayList<tstzspan>();
		lst.add(new tstzspan("[2019-09-01, 2019-09-02]"));
		lst.add(new tstzspan("[2019-09-03, 2019-09-04]"));
		tstzspanset pset = new tstzspanset(lst);
		System.out.println(pset.toString());
		assertEquals(pset.start_period().toString(), new tstzspan("[2019-09-01, 2019-09-02]").toString());
		assertEquals(pset.end_period().toString(), new tstzspan("[2019-09-03, 2019-09-04]").toString());
	}

	@Test
	public void testCopyConstructor(){
		functions.meos_initialize("UTC", errorHandler);
		tstzspanset new_pset = new tstzspanset(pset.copy());
		assertEquals(this.pset.toString(),new_pset.toString());
	}


	@Test
	public void testTotstzset(){
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.to_period().toString(), new tstzspan("[2019-09-01, 2019-09-04]").toString());
	}


	@Test
	public void testNumTimestamps(){
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.num_timestamps(),4);
		assertEquals(this.pset2.num_timestamps(),3);
	}

	@Test
	public void testStartTimestamps(){
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.start_timestamp(), LocalDateTime.of(2019,9,1,0,0,0));
		assertEquals(this.pset2.start_timestamp(),LocalDateTime.of(2019,9,1,0,0,0));
	}


	@Test
	public void testEndTimestamps(){
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.end_timestamp(),LocalDateTime.of(2019,9,4,0,0,0));
		assertEquals(this.pset2.end_timestamp(),LocalDateTime.of(2019,9,4,0,0,0));
	}


	@Test
	public void testNumtstzsets(){
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.num_periods(),2);
		assertEquals(this.pset2.num_periods(),2);
	}


	@Test
	public void testStarttstzset(){
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.start_period().toString(),new tstzspan("[2019-09-01 00:00:00+00, 2019-09-02 00:00:00+00]").toString());
	}


	@Test
	public void testEndtstzset(){
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.end_period().toString(),new tstzspan("[2019-09-03 00:00:00+00, 2019-09-04 00:00:00+00]").toString());
	}



//	@Test
//	public void testHash(){
//		functions.meos_initialize("UTC", errorHandler);
//		assertEquals(this.pset.hash(),552347465);
//	}


	@ParameterizedTest(name="Test Adjacency method")
	@MethodSource("temporals_adjacent")
	public void testAdjacency(TemporalObject other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.is_adjacent(other), expected);
	}

	@ParameterizedTest(name="Test is contained in method")
	@MethodSource("temporals_iscontained")
	public void testIsContainedIn(TemporalObject other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.is_contained_in(other), expected);

	}


	@ParameterizedTest(name="Test contains method")
	@MethodSource("temporals_contains")
	public void testContains(TemporalObject other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.contains(other), expected);

	}


	@ParameterizedTest(name="Test overlaps method")
	@MethodSource("temporals_overlaps")
	public void testOverlaps(TemporalObject other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.overlaps(other), expected);
	}


	@ParameterizedTest(name="Test is same method")
	@MethodSource("temporals_same")
	public void testIsSame(TemporalObject other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.is_same(other), expected);
	}



	@ParameterizedTest(name="Test is before method")
	@MethodSource("temporals_before")
	public void testIsBefore(TemporalObject other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.is_before(other), expected);
	}



	@ParameterizedTest(name="Test is after method")
	@MethodSource("temporals_after")
	public void testIsAfter(TemporalObject other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.is_after(other), expected);
	}


	@ParameterizedTest(name="Test is over or before method")
	@MethodSource("temporals_overbefore")
	public void testIsOverOrBefore(TemporalObject other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.is_over_or_before(other), expected);

	}


	@ParameterizedTest(name="Test is over or after method")
	@MethodSource("temporals_overafter")
	public void testIsOverOrAfter(TemporalObject other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		assertEquals(this.pset.is_over_or_after(other), expected);

	}



	@ParameterizedTest(name="Test intersection method")
	@MethodSource("intersection")
	public void testIntersection(Time other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		this.pset.intersection(other);
	}

	@ParameterizedTest(name="Test minus method")
	@MethodSource("intersection")
	public void testMinus(Time other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		this.pset.minus(other);
	}

	@ParameterizedTest(name="Test union method")
	@MethodSource("intersection")
	public void testUnion(Time other, boolean expected) throws Exception {
		functions.meos_initialize("UTC", errorHandler);
		this.pset.union(other);
	}


	@ParameterizedTest(name="Test equal method")
	@MethodSource("other")
	public void testEqual(Time t) throws SQLException {
		functions.meos_initialize("UTC", errorHandler);
        assertNotEquals(this.pset, t);
	}


	@ParameterizedTest(name="Test ne method")
	@MethodSource("other")
	public void testNotEqual(Time t) throws SQLException {
		functions.meos_initialize("UTC", errorHandler);
		assertTrue(this.pset.notEquals(t));
	}

	@ParameterizedTest(name="Test lt method")
	@MethodSource("other")
	public void testLessThan(Time t) throws SQLException, OperationNotSupportedException {
		functions.meos_initialize("UTC", errorHandler);
		assertTrue(this.pset.lessThan(t));
	}

	@ParameterizedTest(name="Test le method")
	@MethodSource("other")
	public void testLessThanOrEqual(Time t) throws SQLException, OperationNotSupportedException {
		functions.meos_initialize("UTC", errorHandler);
		assertTrue(this.pset.lessThanOrEqual(t));
	}

	@ParameterizedTest(name="Test gt method")
	@MethodSource("other")
	public void testGreaterThan(Time t) throws SQLException, OperationNotSupportedException {
		functions.meos_initialize("UTC", errorHandler);
		assertTrue(this.pset.greaterThan(t));
	}

	@ParameterizedTest(name="Test ge method")
	@MethodSource("other")
	public void testGreaterThanOrEqual(Time t) throws SQLException, OperationNotSupportedException {
		functions.meos_initialize("UTC", errorHandler);
		assertFalse(this.pset.greaterThanOrEqual(t));
	}

}
