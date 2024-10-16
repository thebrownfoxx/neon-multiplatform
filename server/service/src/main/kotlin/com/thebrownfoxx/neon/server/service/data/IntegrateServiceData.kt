package com.thebrownfoxx.neon.server.service.data

//import com.thebrownfoxx.neon.server.service.data.model.ChatGroupRecord
//import com.thebrownfoxx.neon.server.service.data.model.CommunityRecord
//import com.thebrownfoxx.neon.server.service.data.model.ServiceData
//import com.thebrownfoxx.neon.server.service.group.GroupManager
//import com.thebrownfoxx.neon.server.service.member.MemberManager
//import com.thebrownfoxx.neon.server.service.messenger.Messenger
//
//suspend fun ServiceData.integrate(
//    groupManager: GroupManager,
//    memberManager: MemberManager,
//    messenger: Messenger,
//) {
//    val communityRecords = groupRecords.filterIsInstance<CommunityRecord>()
//
//    for (communityRecord in communityRecords) {
//        groupManager.createCommunity(
//            name = communityRecord.group.name,
//            inviteCode = communityRecord.inviteCode,
//        )
//    }
//
//    for (memberRecords in memberRecords) {
//        memberManager.registerMember(
//            inviteCode = memberRecords.inviteCode,
//            username = memberRecords.member.username,
//            password = memberRecords.password,
//        )
//    }
//
//    val chatGroupRecords = groupRecords.filterIsInstance<ChatGroupRecord>()
//
//    for (chatGroupRecord in chatGroupRecords) {
//        messenger.newConversation(chatGroupRecord.memberIds)
//    }
//
//    for (message in messages) {
//        messenger.sendMessage(message.groupId, message.content)
//    }
//}