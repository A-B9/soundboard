package com.soundboard.soundboard.unit.mapper;

import com.soundboard.soundboard.mapper.IMapper;
import com.soundboard.soundboard.mapper.IMapperImpl;
import com.soundboard.soundboard.models.Role;
import com.soundboard.soundboard.models.SoundDTO;
import com.soundboard.soundboard.models.SoundEntity;
import com.soundboard.soundboard.models.UserDTO;
import com.soundboard.soundboard.models.Users;
import com.soundboard.soundboard.models.responseModels.sound.GetSoundResponse;
import com.soundboard.soundboard.util.SoundCategoryEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {IMapperImpl.class})
class TestMapper {

    @Autowired
    IMapper mapper;

    // -----------------------------------------------------------------------
    // toGetResponse(SoundEntity entity)
    // -----------------------------------------------------------------------

    @Test
    void toGetResponse_allFieldsPopulated_mapsEveryTargetField() {
        Instant now = Instant.parse("2024-06-01T10:00:00Z");
        Instant recent = Instant.parse("2024-06-15T12:00:00Z");

        SoundEntity entity = SoundEntity.builder()
                .name("Tavern Ambience")
                .description("Background sounds for a tavern")
                .ownedBy("dungeon_master")
                .contentType("audio/mpeg")
                .storedName("abc123.mp3")
                .size(204800L)
                .createdAt(now)
                .build();
        UUID id1 = UUID.fromString("00000000-0000-0000-0000-000000000042");
        entity.setId(id1);
        entity.setCategory(SoundCategoryEnum.TAVERN);
        entity.setTags(List.of("ambient", "indoors"));
        entity.setRecentUpdate(recent);

        GetSoundResponse response = mapper.toGetResponse(entity);

        assertThat(response.id()).isEqualTo(id1);
        assertThat(response.name()).isEqualTo("Tavern Ambience");
        assertThat(response.description()).isEqualTo("Background sounds for a tavern");
        assertThat(response.ownedBy()).isEqualTo("dungeon_master");
        assertThat(response.category()).isEqualTo(SoundCategoryEnum.TAVERN);
        assertThat(response.tags()).containsExactly("ambient", "indoors");
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.recentUpdate()).isEqualTo(recent);
    }

    @Test
    void toGetResponse_nullableFieldsNull_mandatoryFieldsStillMap() {
        Instant now = Instant.parse("2024-01-01T08:00:00Z");

        SoundEntity entity = SoundEntity.builder()
                .name("Battle Theme")
                .description("Epic battle music")
                .ownedBy("warrior")
                .contentType("audio/wav")
                .storedName("battle.wav")
                .size(1024L)
                .createdAt(now)
                .build();
        UUID id2 = UUID.fromString("00000000-0000-0000-0000-000000000007");
        entity.setId(id2);
        // category, tags, recentUpdate intentionally left null

        GetSoundResponse response = mapper.toGetResponse(entity);

        assertThat(response.id()).isEqualTo(id2);
        assertThat(response.name()).isEqualTo("Battle Theme");
        assertThat(response.description()).isEqualTo("Epic battle music");
        assertThat(response.ownedBy()).isEqualTo("warrior");
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.category()).isNull();
        assertThat(response.tags()).isNull();
        assertThat(response.recentUpdate()).isNull();
    }

    @Test
    void toGetResponse_tagsList_mapsAllTagsInOrder() {
        SoundEntity entity = SoundEntity.builder()
                .name("Combat")
                .description("Intense combat loop")
                .ownedBy("gm_user")
                .contentType("audio/mpeg")
                .storedName("combat.mp3")
                .size(512L)
                .createdAt(Instant.now())
                .build();
        entity.setTags(List.of("action", "combat"));

        GetSoundResponse response = mapper.toGetResponse(entity);

        assertThat(response.tags()).hasSize(2);
        assertThat(response.tags()).containsExactly("action", "combat");
    }

    @Test
    void toGetResponse_presentFields_mapCorrectlyRegardlessOfUnmappedEntityFields() {
        // storedName and contentType exist on SoundEntity but are NOT fields on GetSoundResponse.
        // This test verifies the fields that ARE present on the response map correctly from
        // a fully populated entity — implicitly confirming the record shape excludes the extras.
        Instant now = Instant.parse("2024-03-10T09:00:00Z");

        SoundEntity entity = SoundEntity.builder()
                .name("City Rain")
                .description("Rainy city atmosphere")
                .ownedBy("narrator")
                .contentType("audio/ogg")
                .storedName("city_rain_stored.ogg")
                .size(99999L)
                .createdAt(now)
                .build();
        UUID id3 = UUID.fromString("00000000-0000-0000-0000-000000000003");
        entity.setId(id3);
        entity.setCategory(SoundCategoryEnum.CITY);

        GetSoundResponse response = mapper.toGetResponse(entity);

        assertThat(response.id()).isEqualTo(id3);
        assertThat(response.name()).isEqualTo("City Rain");
        assertThat(response.description()).isEqualTo("Rainy city atmosphere");
        assertThat(response.ownedBy()).isEqualTo("narrator");
        assertThat(response.category()).isEqualTo(SoundCategoryEnum.CITY);
        assertThat(response.createdAt()).isEqualTo(now);
    }

    // -----------------------------------------------------------------------
    // toSoundDTO(SoundEntity entity)
    // -----------------------------------------------------------------------

    @Test
    void toSoundDTO_allFieldsPopulated_mapsEveryTargetField() {
        Instant now = Instant.parse("2024-07-20T14:00:00Z");
        Instant recent = Instant.parse("2024-07-25T16:00:00Z");

        SoundEntity entity = SoundEntity.builder()
                .name("Epic Finale")
                .description("Climactic orchestral piece")
                .ownedBy("composer")
                .contentType("audio/mpeg")
                .storedName("epic_finale.mp3")
                .size(409600L)
                .createdAt(now)
                .build();
        UUID id4 = UUID.fromString("00000000-0000-0000-0000-000000000099");
        entity.setId(id4);
        entity.setCategory(SoundCategoryEnum.EPIC);
        entity.setTags(List.of("orchestral", "climax", "finale"));
        entity.setRecentUpdate(recent);

        SoundDTO dto = mapper.toSoundDTO(entity);

        assertThat(dto.id()).isEqualTo(id4);
        assertThat(dto.name()).isEqualTo("Epic Finale");
        assertThat(dto.description()).isEqualTo("Climactic orchestral piece");
        assertThat(dto.ownedBy()).isEqualTo("composer");
        assertThat(dto.category()).isEqualTo(SoundCategoryEnum.EPIC);
        assertThat(dto.tags()).containsExactly("orchestral", "climax", "finale");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.recentUpdate()).isEqualTo(recent);
    }

    @Test
    void toSoundDTO_nullableFieldsNull_mandatoryFieldsStillMap() {
        Instant now = Instant.parse("2024-02-14T06:30:00Z");

        SoundEntity entity = SoundEntity.builder()
                .name("Travel Music")
                .description("Open road journey tune")
                .ownedBy("traveller")
                .contentType("audio/wav")
                .storedName("travel.wav")
                .size(2048L)
                .createdAt(now)
                .build();
        UUID id5 = UUID.fromString("00000000-0000-0000-0000-000000000005");
        entity.setId(id5);
        // category, tags, recentUpdate intentionally left null

        SoundDTO dto = mapper.toSoundDTO(entity);

        assertThat(dto.id()).isEqualTo(id5);
        assertThat(dto.name()).isEqualTo("Travel Music");
        assertThat(dto.description()).isEqualTo("Open road journey tune");
        assertThat(dto.ownedBy()).isEqualTo("traveller");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.category()).isNull();
        assertThat(dto.tags()).isNull();
        assertThat(dto.recentUpdate()).isNull();
    }

    @Test
    void toSoundDTO_ownedBy_mapsCorrectlyFromEntityToDto() {
        SoundEntity entity = SoundEntity.builder()
                .name("Tense Encounter")
                .description("Suspenseful ambience")
                .ownedBy("specific_owner_value")
                .contentType("audio/mpeg")
                .storedName("tense.mp3")
                .size(300L)
                .createdAt(Instant.now())
                .build();

        SoundDTO dto = mapper.toSoundDTO(entity);

        assertThat(dto.ownedBy()).isEqualTo("specific_owner_value");
    }

    // -----------------------------------------------------------------------
    // toUserDTO(Users user)
    // -----------------------------------------------------------------------

    @Test
    void toUserDTO_mapsRoleField() {
        // An explicitly set ADMIN role must survive the mapping unchanged.
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000011");
        Users user = Users.builder()
                .id(userId)
                .username("admin_user")
                .displayName("Admin")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .role(Role.ADMIN)
                .build();

        UserDTO dto = mapper.toUserDTO(user);

        assertThat(dto.role()).isEqualTo(Role.ADMIN);
        assertThat(dto.username()).isEqualTo("admin_user");
        assertThat(dto.displayName()).isEqualTo("Admin");
        assertThat(dto.id()).isEqualTo(userId);
    }

    @Test
    void toUserDTO_defaultRole_isUser() {
        // When no role is passed to the builder the @Builder.Default kicks in and
        // produces Role.USER — the mapper must carry that value through to the DTO.
        Users user = Users.builder()
                .username("regular_user")
                .displayName("Regular")
                .createdAt(Instant.parse("2025-03-15T12:00:00Z"))
                .build();

        UserDTO dto = mapper.toUserDTO(user);

        assertThat(dto.role()).isEqualTo(Role.USER);
        assertThat(dto.username()).isEqualTo("regular_user");
    }
}
