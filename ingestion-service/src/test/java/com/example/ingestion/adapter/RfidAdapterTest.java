package com.example.ingestion.adapter;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.common.model.CommonEvent;

class RfidAdapterTest {

    private final RfidAdapter adapter = new RfidAdapter();

    @Nested
    @DisplayName("supports() behavior")
    class SupportsTests {

        @Test
        void shouldReturnTrueForExactRfid() {
            assertTrue(adapter.supports("RFID"));
        }

        @Test
        void shouldReturnTrueIgnoringCase() {
            assertTrue(adapter.supports("rfid"));
            assertTrue(adapter.supports("Rfid"));
        }

        @Test
        void shouldReturnFalseForNullVendorType() {
            assertFalse(adapter.supports(null));
        }

        @Test
        void shouldReturnFalseForUnsupportedVendorType() {
            assertFalse(adapter.supports("IOT_DEVICE"));
            assertFalse(adapter.supports("TEMP_SENSOR"));
            assertFalse(adapter.supports("BARCODE"));
        }
    }

    @Nested
    @DisplayName("adapt() success scenarios")
    class SuccessScenarios {

        @Test
        void shouldAdaptFullPayloadSuccessfully() {
            Map<String, Object> payload = Map.ofEntries(
                    entry("tagId", "TAG-1001"),
                    entry("zone", "RECEIVING"),
                    entry("epc", "EPC-ABC-123"),
                    entry("readerId", "READER-01"),
                    entry("eventType", "SCAN"),
                    entry("antennaPort", 3),
                    entry("readCount", 12),
                    entry("rssi", -71.8),
                    entry("timestamp", "2026-04-16T10:15:30Z"),
                    entry("location", Map.of(
                            "storeId", "STORE-ATL-01",
                            "aisle", "A12"
                    )),
                    entry("simulateFailure", true)
            );

            CommonEvent event = adapter.adapt(payload);

            assertNotNull(event);
            assertNotNull(event.getEventId());
            assertFalse(event.getEventId().isBlank());

            assertEquals("TAG-1001", event.getDeviceId());
            assertEquals("SCAN", event.getEventType());
            assertNotNull(event.getTimestamp());

            Map<String, Object> result = event.getPayload();
            assertNotNull(result);

            assertEquals("TAG-1001", result.get("tagId"));
            assertEquals("RECEIVING", result.get("zone"));
            assertEquals("EPC-ABC-123", result.get("epc"));
            assertEquals("READER-01", result.get("readerId"));
            assertEquals(3, result.get("antennaPort"));
            assertEquals(12, result.get("readCount"));
            assertEquals(-71.8, result.get("rssi"));
            assertEquals("STORE-ATL-01", result.get("storeId"));
            assertEquals("A12", result.get("aisle"));
            assertEquals("SCAN", result.get("eventType"));
            assertEquals("v1", result.get("schemaVersion"));
            assertEquals(true, result.get("simulateFailure"));
        }

        @Test
        void shouldDefaultEventTypeInCommonEventWhenMissing() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1002",
                    "zone", "CHECKOUT"
            );

            CommonEvent event = adapter.adapt(payload);

            assertEquals("RFID_SCAN", event.getEventType());
            assertEquals("SCAN", event.getPayload().get("eventType"));
        }

        @Test
        void shouldUseLocalDateTimeTimestampWhenOffsetTimestampIsProvided() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1003",
                    "zone", "STORAGE",
                    "timestamp", "2026-04-16T10:15:30Z"
            );

            CommonEvent event = adapter.adapt(payload);

            assertEquals(LocalDateTime.of(2026, 4, 16, 10, 15, 30), event.getTimestamp());
        }

        @Test
        void shouldUseLocalDateTimeTimestampWhenLocalTimestampIsProvided() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1004",
                    "zone", "BACKROOM",
                    "timestamp", "2026-04-16T10:15:30"
            );

            CommonEvent event = adapter.adapt(payload);

            assertEquals(LocalDateTime.of(2026, 4, 16, 10, 15, 30), event.getTimestamp());
        }

        @Test
        void shouldUseCurrentTimeWhenTimestampIsMissing() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1005",
                    "zone", "SALES_FLOOR"
            );

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);
            CommonEvent event = adapter.adapt(payload);
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            assertNotNull(event.getTimestamp());
            assertTrue(!event.getTimestamp().isBefore(before) && !event.getTimestamp().isAfter(after));
        }

        @Test
        void shouldAcceptNumericFieldsPassedAsStrings() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1006",
                    "zone", "DOCK",
                    "antennaPort", "2",
                    "readCount", "20",
                    "rssi", "-65.25"
            );

            CommonEvent event = adapter.adapt(payload);

            Map<String, Object> result = event.getPayload();

            assertEquals(2, result.get("antennaPort"));
            assertEquals(20, result.get("readCount"));
            assertEquals(-65.25, result.get("rssi"));
        }

        @Test
        void shouldHandleMissingOptionalFieldsGracefully() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1007",
                    "zone", "EXIT"
            );

            CommonEvent event = adapter.adapt(payload);
            Map<String, Object> result = event.getPayload();

            assertEquals("TAG-1007", result.get("tagId"));
            assertEquals("EXIT", result.get("zone"));
            assertNull(result.get("epc"));
            assertNull(result.get("readerId"));
            assertNull(result.get("antennaPort"));
            assertNull(result.get("readCount"));
            assertNull(result.get("rssi"));
            assertNull(result.get("storeId"));
            assertNull(result.get("aisle"));
            assertEquals("v1", result.get("schemaVersion"));
        }

        @Test
        void shouldHandleMissingLocationObjectGracefully() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1008",
                    "zone", "PACKING",
                    "readerId", "READER-02"
            );

            CommonEvent event = adapter.adapt(payload);
            Map<String, Object> result = event.getPayload();

            assertNull(result.get("storeId"));
            assertNull(result.get("aisle"));
        }

        @Test
        void shouldGenerateUniqueEventIdForEachAdaptCall() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1009",
                    "zone", "ZONE-X"
            );

            CommonEvent event1 = adapter.adapt(payload);
            CommonEvent event2 = adapter.adapt(payload);

            assertNotNull(event1.getEventId());
            assertNotNull(event2.getEventId());
            assertNotEquals(event1.getEventId(), event2.getEventId());
        }
    }

    @Nested
    @DisplayName("adapt() validation failures")
    class ValidationFailures {

        @Test
        void shouldThrowExceptionWhenPayloadIsNull() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(null)
            );

            assertEquals("RFID payload cannot be null or empty", ex.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenPayloadIsEmpty() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(Map.of())
            );

            assertEquals("RFID payload cannot be null or empty", ex.getMessage());
        }

        @Test
        void shouldThrowExceptionWhenTagIdIsMissing() {
            Map<String, Object> payload = Map.of(
                    "zone", "ZONE-1"
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(payload)
            );

            assertTrue(ex.getMessage().contains("tagId"));
        }

        @Test
        void shouldThrowExceptionWhenZoneIsMissing() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1010"
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(payload)
            );

            assertTrue(ex.getMessage().contains("zone"));
        }

        @Test
        void shouldThrowExceptionWhenTagIdIsBlank() {
            Map<String, Object> payload = Map.of(
                    "tagId", "   ",
                    "zone", "ZONE-1"
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(payload)
            );

            assertTrue(ex.getMessage().contains("tagId"));
        }

        @Test
        void shouldThrowExceptionWhenZoneIsBlank() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1011",
                    "zone", "   "
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(payload)
            );

            assertTrue(ex.getMessage().contains("zone"));
        }
    }

    @Nested
    @DisplayName("adapt() parsing failures")
    class ParsingFailures {

        @Test
        void shouldThrowExceptionWhenTimestampFormatIsInvalid() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1012",
                    "zone", "ZONE-2",
                    "timestamp", "16-04-2026 10:15:30"
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(payload)
            );

            assertTrue(ex.getMessage().contains("Invalid timestamp format"));
        }

        @Test
        void shouldThrowExceptionWhenAntennaPortIsInvalid() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1013",
                    "zone", "ZONE-2",
                    "antennaPort", "abc"
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(payload)
            );

            assertTrue(ex.getMessage().contains("antennaPort"));
        }

        @Test
        void shouldThrowExceptionWhenReadCountIsInvalid() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1014",
                    "zone", "ZONE-2",
                    "readCount", "xyz"
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(payload)
            );

            assertTrue(ex.getMessage().contains("readCount"));
        }

        @Test
        void shouldThrowExceptionWhenRssiIsInvalid() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1015",
                    "zone", "ZONE-2",
                    "rssi", "bad-double"
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(payload)
            );

            assertTrue(ex.getMessage().contains("rssi"));
        }

        @Test
        void shouldThrowExceptionWhenLocationIsNotAMap() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-1016",
                    "zone", "ZONE-2",
                    "location", "not-a-map"
            );

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> adapter.adapt(payload)
            );

            assertTrue(ex.getMessage().contains("location"));
        }
    }

    @Nested
    @DisplayName("contract and mapping checks")
    class ContractChecks {

        @Test
        void shouldAlwaysMapDeviceIdFromTagId() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-2001",
                    "zone", "RETAIL"
            );

            CommonEvent event = adapter.adapt(payload);

            assertEquals("TAG-2001", event.getDeviceId());
        }

        @Test
        void shouldAlwaysIncludeSchemaVersionV1() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-2002",
                    "zone", "RETAIL"
            );

            CommonEvent event = adapter.adapt(payload);

            assertEquals("v1", event.getPayload().get("schemaVersion"));
        }

        @Test
        void shouldPreserveExplicitEventTypeInBothPlaces() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-2003",
                    "zone", "RETAIL",
                    "eventType", "EXIT_SCAN"
            );

            CommonEvent event = adapter.adapt(payload);

            assertEquals("EXIT_SCAN", event.getEventType());
            assertEquals("EXIT_SCAN", event.getPayload().get("eventType"));
        }

        @Test
        void shouldKeepSimulateFailureFlagForDownstreamProcessing() {
            Map<String, Object> payload = Map.of(
                    "tagId", "TAG-2004",
                    "zone", "RETAIL",
                    "simulateFailure", false
            );

            CommonEvent event = adapter.adapt(payload);

            assertEquals(false, event.getPayload().get("simulateFailure"));
        }
    }
}