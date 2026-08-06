package com.example.personalvault.ui.vault

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personalvault.model.*
import com.example.personalvault.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryFormDialog(
    initialSection: SectionType?,
    editingEntry: VaultEntry?,
    onDismiss: () -> Unit,
    onSave: (VaultEntry) -> Unit,
    onOpenGenerator: () -> Unit
) {
    var selectedSection by remember {
        mutableStateOf(editingEntry?.sectionType ?: initialSection ?: SectionType.PASSWORD)
    }

    // Password Fields
    var passwordTitle by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.accountName ?: "") }
    var passwordUsername by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.username ?: "") }
    var passwordValue by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.passwordValue ?: "") }
    var passwordUrl by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.url ?: "") }
    var passwordProvider by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.signInProvider ?: SignInProvider.NONE) }
    var passwordCategory by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.category ?: "General") }
    var passwordIsFavorite by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.isFavorite ?: false) }
    var passwordNotes by remember { mutableStateOf((editingEntry as? VaultEntry.Password)?.additionalInfo ?: "") }
    var showPasswordVal by remember { mutableStateOf(false) }

    // Document Fields
    var docName by remember { mutableStateOf((editingEntry as? VaultEntry.Document)?.documentName ?: "") }
    var docType by remember { mutableStateOf((editingEntry as? VaultEntry.Document)?.documentType ?: DocumentType.OTHER) }
    var docDesc by remember { mutableStateOf((editingEntry as? VaultEntry.Document)?.description ?: "") }
    var docTags by remember { mutableStateOf((editingEntry as? VaultEntry.Document)?.tags?.joinToString(", ") ?: "") }

    // Driving License Fields
    var licName by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.licenseName ?: "") }
    var licHolder by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.holderName ?: "") }
    var licNumber by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.licenseNumber ?: "") }
    var licType by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.licenseType ?: LicenseType.NON_PROFESSIONAL) }
    var licIssue by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.issueDate ?: "") }
    var licExpiry by remember { mutableStateOf((editingEntry as? VaultEntry.DrivingLicense)?.expiryDate ?: "") }

    // Certificate Fields
    var certName by remember { mutableStateOf((editingEntry as? VaultEntry.Certificate)?.certificateName ?: "") }
    var certType by remember { mutableStateOf((editingEntry as? VaultEntry.Certificate)?.certificateType ?: CertificateType.OTHER) }
    var certInst by remember { mutableStateOf((editingEntry as? VaultEntry.Certificate)?.institutionName ?: "") }
    var certYear by remember { mutableStateOf((editingEntry as? VaultEntry.Certificate)?.yearOfCompletion ?: "") }
    var certDesc by remember { mutableStateOf((editingEntry as? VaultEntry.Certificate)?.description ?: "") }

    // ID Card Fields
    var idName by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.cardName ?: "") }
    var idType by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.cardType ?: IdCardType.NATIONAL_ID) }
    var idNumber by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.cardNumber ?: "") }
    var idHolder by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.holderName ?: "") }
    var idIssue by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.issueDate ?: "") }
    var idExpiry by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.expiryDate ?: "") }
    var idDesc by remember { mutableStateOf((editingEntry as? VaultEntry.IdCard)?.description ?: "") }

    // Bank Fields
    var bankName by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.bankName ?: "") }
    var bankType by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.bankType ?: BankAccountType.SAVINGS) }
    var bankBranch by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.branchName ?: "") }
    var bankAccountNum by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.accountNumber ?: "") }
    var bankHolder by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.accountHolderName ?: "") }
    var bankRouting by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.routingNumber ?: "") }
    var bankSwift by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.swiftCode ?: "") }
    var bankIfsc by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.ifscCode ?: "") }
    var bankNotes by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.additionalInfo ?: "") }
    var bankSignature by remember { mutableStateOf<EncryptedFileAttachment?>((editingEntry as? VaultEntry.Bank)?.signatureFile) }
    var bankCards by remember { mutableStateOf((editingEntry as? VaultEntry.Bank)?.cards ?: emptyList()) }

    val context = LocalContext.current

    var attachedFiles by remember {
        mutableStateOf<List<EncryptedFileAttachment>>(
            when (editingEntry) {
                is VaultEntry.Password -> editingEntry.attachments
                is VaultEntry.Document -> editingEntry.files
                is VaultEntry.DrivingLicense -> editingEntry.files
                is VaultEntry.Certificate -> editingEntry.files
                is VaultEntry.IdCard -> editingEntry.files
                is VaultEntry.Bank -> editingEntry.signatureFile?.let { listOf(it) } ?: emptyList()
                null -> emptyList()
            }
        )
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    var fileName = "attachment_${System.currentTimeMillis()}"
                    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"

                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex != -1) {
                                fileName = cursor.getString(nameIndex)
                            }
                        }
                    }

                    val base64Data = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    val attachment = EncryptedFileAttachment(
                        fileName = fileName,
                        mimeType = mimeType,
                        size = bytes.size.toLong(),
                        encryptedData = base64Data,
                        iv = ""
                    )
                    attachedFiles = attachedFiles + attachment
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val signaturePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    var fileName = "signature_${System.currentTimeMillis()}"
                    val mimeType = contentResolver.getType(uri) ?: "image/png"

                    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIndex != -1) {
                                fileName = cursor.getString(nameIndex)
                            }
                        }
                    }

                    val base64Data = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    bankSignature = EncryptedFileAttachment(
                        fileName = fileName,
                        mimeType = mimeType,
                        size = bytes.size.toLong(),
                        encryptedData = base64Data,
                        iv = ""
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var cardTargetIndexForPicker by remember { mutableStateOf<Int?>(null) }

    val cardImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val targetIdx = cardTargetIndexForPicker
            if (targetIdx != null && targetIdx in bankCards.indices) {
                try {
                    val contentResolver = context.contentResolver
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()

                    if (bytes != null) {
                        var fileName = "card_${System.currentTimeMillis()}"
                        val mimeType = contentResolver.getType(uri) ?: "image/png"

                        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            if (cursor.moveToFirst()) {
                                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (nameIndex != -1) {
                                    fileName = cursor.getString(nameIndex)
                                }
                            }
                        }

                        val base64Data = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                        val attachment = EncryptedFileAttachment(
                            fileName = fileName,
                            mimeType = mimeType,
                            size = bytes.size.toLong(),
                            encryptedData = base64Data,
                            iv = ""
                        )
                        val targetCard = bankCards[targetIdx]
                        val updatedCard = targetCard.copy(
                            attachments = targetCard.attachments + attachment
                        )
                        bankCards = bankCards.toMutableList().apply {
                            this[targetIdx] = updatedCard
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VaultBgSurface,
        title = {
            Text(
                text = if (editingEntry != null) "Edit Entry" else "New Vault Entry",
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (editingEntry == null) {
                    // Category selector chips
                    Text("Select Category", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SectionType.values().forEach { sec ->
                            FilterChip(
                                selected = selectedSection == sec,
                                onClick = { selectedSection = sec },
                                label = { Text(sec.label, fontSize = 12.sp, maxLines = 1) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentPrimary,
                                    selectedLabelColor = TextPrimary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when (selectedSection) {
                    SectionType.PASSWORD -> {
                        OutlinedTextField(
                            value = passwordTitle,
                            onValueChange = { passwordTitle = it },
                            label = { Text("Account Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordUsername,
                            onValueChange = { passwordUsername = it },
                            label = { Text("Username / Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = passwordValue,
                                onValueChange = { passwordValue = it },
                                label = { Text("Password") },
                                singleLine = true,
                                visualTransformation = if (showPasswordVal) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPasswordVal = !showPasswordVal }) {
                                        Icon(
                                            imageVector = if (showPasswordVal) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(onClick = onOpenGenerator) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Generator",
                                    tint = AccentPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordUrl,
                            onValueChange = { passwordUrl = it },
                            label = { Text("Website / App URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sign-in Method", fontSize = 12.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SignInProvider.values().forEach { provider ->
                                FilterChip(
                                    selected = passwordProvider == provider,
                                    onClick = { passwordProvider = provider },
                                    label = {
                                        Text(
                                            when (provider) {
                                                SignInProvider.NONE -> "Direct / Password"
                                                SignInProvider.GOOGLE -> "Google"
                                                SignInProvider.MICROSOFT -> "Microsoft"
                                                SignInProvider.FACEBOOK -> "Facebook"
                                                SignInProvider.APPLE -> "Apple"
                                            },
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentPrimary,
                                        selectedLabelColor = TextPrimary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordNotes,
                            onValueChange = { passwordNotes = it },
                            label = { Text("Additional Info / Notes") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = passwordIsFavorite,
                                onCheckedChange = { passwordIsFavorite = it }
                            )
                            Text("Mark as Favorite", color = TextPrimary, fontSize = 13.sp)
                        }
                    }

                    SectionType.DOCUMENT -> {
                        OutlinedTextField(
                            value = docName,
                            onValueChange = { docName = it },
                            label = { Text("Document Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Document Type", fontSize = 12.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DocumentType.values().forEach { type ->
                                FilterChip(
                                    selected = docType == type,
                                    onClick = { docType = type },
                                    label = { Text(type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentPrimary,
                                        selectedLabelColor = TextPrimary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = docDesc,
                            onValueChange = { docDesc = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = docTags,
                            onValueChange = { docTags = it },
                            label = { Text("Tags (comma separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    SectionType.DRIVING_LICENSE -> {
                        OutlinedTextField(
                            value = licName,
                            onValueChange = { licName = it },
                            label = { Text("License Title *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = licHolder,
                            onValueChange = { licHolder = it },
                            label = { Text("Holder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = licNumber,
                            onValueChange = { licNumber = it },
                            label = { Text("License Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("License Type", fontSize = 12.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LicenseType.values().forEach { type ->
                                FilterChip(
                                    selected = licType == type,
                                    onClick = { licType = type },
                                    label = { Text(type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentPrimary,
                                        selectedLabelColor = TextPrimary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = licIssue,
                                onValueChange = { licIssue = it },
                                label = { Text("Issue Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = licExpiry,
                                onValueChange = { licExpiry = it },
                                label = { Text("Expiry Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    SectionType.CERTIFICATE -> {
                        OutlinedTextField(
                            value = certName,
                            onValueChange = { certName = it },
                            label = { Text("Certificate Title *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Certificate Type", fontSize = 12.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CertificateType.values().forEach { type ->
                                FilterChip(
                                    selected = certType == type,
                                    onClick = { certType = type },
                                    label = { Text(type.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentPrimary,
                                        selectedLabelColor = TextPrimary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = certInst,
                            onValueChange = { certInst = it },
                            label = { Text("Institution / Organization") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = certYear,
                            onValueChange = { certYear = it },
                            label = { Text("Year of Completion") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = certDesc,
                            onValueChange = { certDesc = it },
                            label = { Text("Description / Details") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    SectionType.ID_CARD -> {
                        OutlinedTextField(
                            value = idName,
                            onValueChange = { idName = it },
                            label = { Text("ID Card Title / Card Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Card Type", fontSize = 12.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IdCardType.values().forEach { type ->
                                FilterChip(
                                    selected = idType == type,
                                    onClick = { idType = type },
                                    label = { Text(type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentPrimary,
                                        selectedLabelColor = TextPrimary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = idNumber,
                            onValueChange = { idNumber = it },
                            label = { Text("Card / Document Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = idHolder,
                            onValueChange = { idHolder = it },
                            label = { Text("Holder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = idIssue,
                                onValueChange = { idIssue = it },
                                label = { Text("Issue Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = idExpiry,
                                onValueChange = { idExpiry = it },
                                label = { Text("Expiry Date") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = idDesc,
                            onValueChange = { idDesc = it },
                            label = { Text("Description / Details") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    SectionType.BANK -> {
                        OutlinedTextField(
                            value = bankName,
                            onValueChange = { bankName = it },
                            label = { Text("Bank Name *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Account Type", fontSize = 12.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BankAccountType.values().forEach { type ->
                                FilterChip(
                                    selected = bankType == type,
                                    onClick = { bankType = type },
                                    label = { Text(type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentPrimary,
                                        selectedLabelColor = TextPrimary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = bankBranch,
                            onValueChange = { bankBranch = it },
                            label = { Text("Branch Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = bankAccountNum,
                            onValueChange = { bankAccountNum = it },
                            label = { Text("Account Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = bankHolder,
                            onValueChange = { bankHolder = it },
                            label = { Text("Account Holder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bankRouting,
                                onValueChange = { bankRouting = it },
                                label = { Text("Routing Number") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = bankSwift,
                                onValueChange = { bankSwift = it },
                                label = { Text("SWIFT Code") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = bankIfsc,
                            onValueChange = { bankIfsc = it },
                            label = { Text("IFSC Code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = bankNotes,
                            onValueChange = { bankNotes = it },
                            label = { Text("Additional Info / Notes") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Bank Signature Attachment
                        Text("Bank Signature", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (bankSignature == null) {
                            OutlinedButton(
                                onClick = { signaturePickerLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Bank Signature / File")
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(VaultBgCard, RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(bankSignature?.fileName ?: "Signature", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                        Text("${(bankSignature?.size ?: 0) / 1024} KB", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }
                                IconButton(onClick = { bankSignature = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        // Bank Cards Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bank Cards", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            }
                            Button(
                                onClick = {
                                    bankCards = bankCards + BankCard()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Card", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Card", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (bankCards.isEmpty()) {
                            Text("No bank cards added yet. Click 'Add Card' to attach a card.", fontSize = 12.sp, color = TextSecondary)
                        } else {
                            bankCards.forEachIndexed { index, card ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .background(VaultBgCard, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Card #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        IconButton(
                                            onClick = {
                                                bankCards = bankCards.toMutableList().apply { removeAt(index) }
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove Card", tint = TextSecondary, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        var expanded by remember { mutableStateOf(false) }
                                        Box(modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = card.cardType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Card Type") },
                                                trailingIcon = {
                                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .clickable { expanded = true }
                                            )
                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                BankCardType.values().forEach { type ->
                                                    DropdownMenuItem(
                                                        text = { Text(type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                                                        onClick = {
                                                            bankCards = bankCards.toMutableList().apply {
                                                                this[index] = card.copy(cardType = type)
                                                            }
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedTextField(
                                            value = card.cardNumber,
                                            onValueChange = { newNum ->
                                                bankCards = bankCards.toMutableList().apply {
                                                    this[index] = card.copy(cardNumber = newNum)
                                                }
                                            },
                                            label = { Text("Card Number") },
                                            placeholder = { Text("XXXX-XXXX-XXXX-XXXX") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = card.pin,
                                            onValueChange = { newPin ->
                                                bankCards = bankCards.toMutableList().apply {
                                                    this[index] = card.copy(pin = newPin)
                                                }
                                            },
                                            label = { Text("PIN") },
                                            placeholder = { Text("....") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )

                                        OutlinedTextField(
                                            value = card.cvv,
                                            onValueChange = { newCvv ->
                                                bankCards = bankCards.toMutableList().apply {
                                                    this[index] = card.copy(cvv = newCvv)
                                                }
                                            },
                                            label = { Text("CVV") },
                                            placeholder = { Text("...") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = card.expiryDate,
                                        onValueChange = { newExp ->
                                            bankCards = bankCards.toMutableList().apply {
                                                this[index] = card.copy(expiryDate = newExp)
                                            }
                                        },
                                        label = { Text("Expiry Date") },
                                        placeholder = { Text("MM/YY") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Card Images & Scans (Front / Back)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedButton(
                                        onClick = {
                                            cardTargetIndexForPicker = index
                                            cardImagePickerLauncher.launch("image/*")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Upload Card Image / Scan", fontSize = 12.sp)
                                    }

                                    val cardFiles = listOfNotNull(card.cardImage) + card.attachments
                                    if (cardFiles.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        cardFiles.forEach { file ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp)
                                                    .background(VaultBgSurface, RoundedCornerShape(8.dp))
                                                     .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Icon(
                                                        imageVector = if (file.mimeType.startsWith("image/")) Icons.Default.Image else Icons.Default.AttachFile,
                                                        contentDescription = null,
                                                        tint = AccentPrimary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        Text(file.fileName, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1)
                                                        Text("${file.size / 1024} KB", fontSize = 9.sp, color = TextSecondary)
                                                    }
                                                }
                                                IconButton(
                                                    onClick = {
                                                        val newAttachments = card.attachments.toMutableList()
                                                        if (file == card.cardImage) {
                                                            bankCards = bankCards.toMutableList().apply {
                                                                this[index] = card.copy(cardImage = null)
                                                            }
                                                        } else {
                                                            newAttachments.remove(file)
                                                            bankCards = bankCards.toMutableList().apply {
                                                                this[index] = card.copy(attachments = newAttachments)
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedSection != SectionType.BANK) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = VaultBgCard)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Attachments & Scans (Optional)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)

                    OutlinedButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload File / Document / Image Scan")
                    }

                    if (attachedFiles.isNotEmpty()) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            attachedFiles.forEachIndexed { index, file ->
                                Surface(
                                    color = VaultBgCard,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Icon(
                                            imageVector = if (file.mimeType.startsWith("image/")) Icons.Default.Image else Icons.Default.AttachFile,
                                            contentDescription = null,
                                            tint = AccentPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(file.fileName, fontSize = 12.sp, color = TextPrimary, maxLines = 1)
                                            Text("${file.size / 1024} KB", fontSize = 10.sp, color = TextSecondary)
                                        }
                                        IconButton(
                                            onClick = { attachedFiles = attachedFiles.filterIndexed { i, _ -> i != index } },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = AccentDanger, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val entryToSave: VaultEntry? = when (selectedSection) {
                        SectionType.PASSWORD -> {
                            if (passwordTitle.isBlank()) null
                            else VaultEntry.Password(
                                id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                accountName = passwordTitle,
                                username = passwordUsername,
                                passwordValue = passwordValue,
                                url = passwordUrl,
                                signInProvider = passwordProvider,
                                category = passwordCategory,
                                isFavorite = passwordIsFavorite,
                                additionalInfo = passwordNotes,
                                attachments = attachedFiles
                            )
                        }
                        SectionType.DOCUMENT -> {
                            if (docName.isBlank()) null
                            else VaultEntry.Document(
                                id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                documentName = docName,
                                documentType = docType,
                                description = docDesc,
                                tags = docTags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                files = attachedFiles
                            )
                        }
                        SectionType.DRIVING_LICENSE -> {
                            if (licName.isBlank()) null
                            else VaultEntry.DrivingLicense(
                                id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                licenseName = licName,
                                holderName = licHolder,
                                licenseNumber = licNumber,
                                licenseType = licType,
                                issueDate = licIssue,
                                expiryDate = licExpiry,
                                files = attachedFiles
                            )
                        }
                        SectionType.CERTIFICATE -> {
                            if (certName.isBlank()) null
                            else VaultEntry.Certificate(
                                id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                certificateName = certName,
                                certificateType = certType,
                                institutionName = certInst,
                                yearOfCompletion = certYear,
                                description = certDesc,
                                files = attachedFiles
                            )
                        }
                        SectionType.ID_CARD -> {
                            if (idName.isBlank()) null
                            else VaultEntry.IdCard(
                                id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                cardName = idName,
                                cardType = idType,
                                cardNumber = idNumber,
                                holderName = idHolder,
                                issueDate = idIssue,
                                expiryDate = idExpiry,
                                description = idDesc,
                                files = attachedFiles
                            )
                        }
                        SectionType.BANK -> {
                            if (bankName.isBlank()) null
                            else {
                                VaultEntry.Bank(
                                    id = editingEntry?.id ?: java.util.UUID.randomUUID().toString(),
                                    createdAt = editingEntry?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis(),
                                    bankName = bankName,
                                    bankType = bankType,
                                    branchName = bankBranch,
                                    accountNumber = bankAccountNum,
                                    accountHolderName = bankHolder,
                                    routingNumber = bankRouting,
                                    swiftCode = bankSwift,
                                    ifscCode = bankIfsc,
                                    additionalInfo = bankNotes,
                                    signatureFile = bankSignature,
                                    cards = bankCards
                                )
                            }
                        }
                    }

                    if (entryToSave != null) {
                        onSave(entryToSave)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
