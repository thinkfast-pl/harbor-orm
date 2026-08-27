// SPDX-License-Identifier: Apache-2.0

package io.github.thinkfastpl.harbororm.test

import io.github.thinkfastpl.harbororm.api.LazyRef
import io.github.thinkfastpl.harbororm.test.domain.MemberEntity
import io.github.thinkfastpl.harbororm.test.domain.MemberProfileEntity
import io.github.thinkfastpl.harbororm.test.domain.QMemberEntity
import io.github.thinkfastpl.harbororm.test.domain.QMemberProfileEntity

class OneToOneRelationIT extends AbstractHarborIT {

    // ==================== Insert Cascade ====================

    def "should insert parent with child via @OneToOne cascade"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            MemberEntity member = new MemberEntity(1L, "Alice", LazyRef.of(new MemberProfileEntity(1L, null, "Alice's bio")))

        when:
            session.insertEntity(qMember, member)

        then:
            session.selectEntity(qMember).count() == 1
            session.selectEntity(qProfile).count() == 1

            MemberProfileEntity profile = session.selectEntity(qProfile).whereIdEq(1L).fetchSingle()
            profile.memberId == 1L
            profile.bio == "Alice's bio"

        where:
            session << allSessions
    }

    def "should insert parent with null LazyRef"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            MemberEntity member = new MemberEntity(1L, "Bob", null)

        when:
            session.insertEntity(qMember, member)

        then:
            session.selectEntity(qMember).count() == 1
            session.selectEntity(qProfile).count() == 0
            session.selectEntity(qMember).where(qMember.id.eq(1L)).fetchSingle().getProfile() != null
            session.selectEntity(qMember).where(qMember.id.eq(1L)).fetchSingle().getProfile().get() == null
            session.selectEntity(qMember).where(qMember.id.eq(1L)).fetchSingle().getProfile().toOptional().isEmpty()

        where:
            session << allSessions
    }

    def "should insert parent with empty LazyRef"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            MemberEntity member = new MemberEntity(1L, "Bob", LazyRef.of(null))

        when:
            session.insertEntity(qMember, member)

        then:
            session.selectEntity(qMember).count() == 1
            session.selectEntity(qProfile).count() == 0
            session.selectEntity(qMember).where(qMember.id.eq(1L)).fetchSingle().getProfile() != null
            session.selectEntity(qMember).where(qMember.id.eq(1L)).fetchSingle().getProfile().get() == null
            session.selectEntity(qMember).where(qMember.id.eq(1L)).fetchSingle().getProfile().toOptional().isEmpty()

        where:
            session << allSessions
    }

    // ==================== Lazy Loading ====================

    def "should lazily load child entity via LazyRef.get()"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))
            session.insertEntity(qProfile, new MemberProfileEntity(1L, 1L, "Alice's bio"))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()

        then:
            member.profile != null
            member.profile.get() != null
            member.profile.get().id == 1L
            member.profile.get().bio == "Alice's bio"

        where:
            session << allSessions
    }

    def "should return null from LazyRef.get() when no child exists"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Bob", null))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()

        then:
            member.profile != null
            member.profile.get() == null

        where:
            session << allSessions
    }

    def "should return correct Optional from LazyRef.toOptional()"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))
            session.insertEntity(qMember, new MemberEntity(2L, "Bob", null))
            session.insertEntity(qProfile, new MemberProfileEntity(1L, 1L, "Alice's bio"))

        when:
            MemberEntity alice = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            MemberEntity bob = session.selectEntity(qMember).whereIdEq(2L).fetchSingle()

        then:
            alice.profile.toOptional().isPresent()
            alice.profile.toOptional().get().bio == "Alice's bio"
            !bob.profile.toOptional().isPresent()

        where:
            session << allSessions
    }

    // ==================== Batch Loading ====================

    def "should batch-load children for multiple parents"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))
            session.insertEntity(qMember, new MemberEntity(2L, "Bob", null))
            session.insertEntity(qMember, new MemberEntity(3L, "Charlie", null))
            session.insertEntity(qProfile, new MemberProfileEntity(1L, 1L, "Alice's bio"))
            session.insertEntity(qProfile, new MemberProfileEntity(2L, 2L, "Bob's bio"))

        when:
            List<MemberEntity> members = session.selectEntity(qMember)
                    .orderBy(qMember.id.asc())
                    .fetchAll()

        then:
            members.size() == 3
            members[0].profile.get() != null
            members[0].profile.get().bio == "Alice's bio"
            members[1].profile.get() != null
            members[1].profile.get().bio == "Bob's bio"
            members[2].profile.get() == null

        where:
            session << allSessions
    }

    // ==================== Update Cascade ====================

    def "should cascade update to child entity"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))
            session.insertEntity(qProfile, new MemberProfileEntity(1L, 1L, "Old bio"))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            member.profile.get().setBio("New bio")
            session.updateEntity(qMember, member)

        then:
            MemberProfileEntity updated = session.selectEntity(qProfile).whereIdEq(1L).fetchSingle()
            updated.bio == "New bio"

        where:
            session << allSessions
    }

    def "should not touch child when LazyRef is not accessed during update"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))
            session.insertEntity(qProfile, new MemberProfileEntity(1L, 1L, "Original bio"))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            member.setName("Alice Updated")
            session.updateEntity(qMember, member)

        then:
            MemberEntity reloaded = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            reloaded.name == "Alice Updated"
            reloaded.profile.get().bio == "Original bio"

        where:
            session << allSessions
    }

    def "should delete child when LazyRef replaced with null-holding ref"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))
            session.insertEntity(qProfile, new MemberProfileEntity(1L, 1L, "Bio to remove"))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            member.profile.get() // trigger lazy load
            member.setProfile(LazyRef.of(null))
            session.updateEntity(qMember, member)

        then:
            session.selectEntity(qProfile).count() == 0

        where:
            session << allSessions
    }

    def "should delete child when LazyRef replaced with null"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))
            session.insertEntity(qProfile, new MemberProfileEntity(1L, 1L, "Bio to remove"))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            member.profile.get() // trigger lazy load
            member.setProfile(null)
            session.updateEntity(qMember, member)

        then:
            session.selectEntity(qProfile).count() == 0

        where:
            session << allSessions
    }

    def "should insert child when adding to previously childless parent"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            member.profile.get() // trigger lazy load, returns null
            member.setProfile(LazyRef.of(new MemberProfileEntity(1L, null, "New bio")))
            session.updateEntity(qMember, member)

        then:
            session.selectEntity(qProfile).count() == 1
            MemberProfileEntity profile = session.selectEntity(qProfile).whereIdEq(1L).fetchSingle()
            profile.bio == "New bio"
            profile.memberId == 1L

        where:
            session << allSessions
    }

    def "should replace child when different entity is set"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))
            session.insertEntity(qProfile, new MemberProfileEntity(1L, 1L, "Old profile"))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            member.profile.get() // trigger lazy load
            member.setProfile(LazyRef.of(new MemberProfileEntity(2L, null, "New profile")))
            session.updateEntity(qMember, member)

        then:
            session.selectEntity(qProfile).count() == 1
            MemberProfileEntity profile = session.selectEntity(qProfile).whereIdEq(2L).fetchSingle()
            profile.bio == "New profile"
            profile.memberId == 1L
            !session.selectEntity(qProfile).whereIdEq(1L).fetchOne().isPresent()

        where:
            session << allSessions
    }

    // ==================== Delete Cascade ====================

    def "should cascade delete to child entity"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))
            session.insertEntity(qProfile, new MemberProfileEntity(1L, 1L, "Alice's bio"))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            session.deleteEntity(qMember, member)

        then:
            session.selectEntity(qMember).count() == 0
            session.selectEntity(qProfile).count() == 0

        where:
            session << allSessions
    }

    def "should delete parent with no child"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            session.insertEntity(qMember, new MemberEntity(1L, "Bob", null))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            session.deleteEntity(qMember, member)

        then:
            session.selectEntity(qMember).count() == 0

        where:
            session << allSessions
    }

    // ==================== Multiple Rows Exception ====================

    def "should throw exception when multiple children found for @OneToOne"() {
        given:
            QMemberEntity qMember = new QMemberEntity(null)
            QMemberProfileEntity qProfile = new QMemberProfileEntity(null)
            // Insert member
            session.insertEntity(qMember, new MemberEntity(1L, "Alice", null))
            // Insert first profile
            session.insertEntity(qProfile, new MemberProfileEntity(1L, 1L, "Bio 1"))
            // Insert second profile with same member_id (no unique constraint in test table)
            session.insertEntity(qProfile, new MemberProfileEntity(2L, 1L, "Bio 2"))

        when:
            MemberEntity member = session.selectEntity(qMember).whereIdEq(1L).fetchSingle()
            member.profile.get()

        then:
            thrown(IllegalStateException)

        where:
            session << allSessions
    }
}
