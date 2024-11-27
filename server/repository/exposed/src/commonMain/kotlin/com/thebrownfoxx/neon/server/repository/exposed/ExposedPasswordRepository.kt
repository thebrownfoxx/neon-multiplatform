//package com.thebrownfoxx.neon.server.repository.exposed
//
//import com.thebrownfoxx.neon.common.data.exposed.ExposedDataSource
//import com.thebrownfoxx.neon.common.hash.Hash
//import com.thebrownfoxx.neon.common.type.Outcome
//import com.thebrownfoxx.neon.common.type.UnitOutcome
//import com.thebrownfoxx.neon.common.type.id.MemberId
//import com.thebrownfoxx.neon.server.repository.PasswordRepository
//import com.thebrownfoxx.neon.server.repository.password.model.RepositoryGetPasswordHashError
//import com.thebrownfoxx.neon.server.repository.password.model.RepositorySetPasswordHashError
//import org.jetbrains.exposed.sql.Database
//import org.jetbrains.exposed.sql.Table
//
//class ExposedPasswordRepository(
//    database: Database,
//) : PasswordRepository, ExposedDataSource(database) {
//    override suspend fun getHash(memberId: MemberId): Outcome<Hash, RepositoryGetPasswordHashError> {
//        TODO("Not yet implemented")
//    }
//
//    // TODO: Make a wrapper for exposed, or even like any generic repository? that will automatically
//    //  handle flows since they are such a pain to write over and over
//
//    override suspend fun setHash(
//        memberId: MemberId,
//        hash: Hash,
//    ): UnitOutcome<RepositorySetPasswordHashError> {
//        TODO("Not yet implemented")
//    }
//
//    override val table: Table
//        get() = TODO("Not yet implemented")
//
//}