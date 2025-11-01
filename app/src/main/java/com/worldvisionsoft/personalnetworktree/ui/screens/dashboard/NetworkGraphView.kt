package com.worldvisionsoft.personalnetworktree.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.worldvisionsoft.personalnetworktree.R
import com.worldvisionsoft.personalnetworktree.data.model.Contact
import com.worldvisionsoft.personalnetworktree.data.repository.ContactRepository
import java.util.Locale

data class NetworkNode(
    val id: String,
    val name: String,
    val position: Offset,
    val photoUrl: String = "",
    val connections: List<String> = emptyList(),
    val color: Color = Color.Blue,
    val level: Int = 1
)

@Composable
fun NetworkGraphView(
    currentUserEmail: String? = null,
    onNodeClick: (String) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { ContactRepository(context) }
    val contacts by repository.contacts.collectAsState(initial = emptyList())

    // Zoom and Pan state
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var showPreview by remember { mutableStateOf(false) }

    // Convert contacts to network nodes
    val nodes = remember(contacts) {
        generateNodesFromContacts(contacts)
    }

    // Compute tree edges: for each level L>1, connect each node to the nearest-by-X node from level L-1
    val levelGroups = remember(nodes) { nodes.groupBy { it.level } }
    val maxLevel = remember(levelGroups) { levelGroups.keys.maxOrNull() ?: 1 }
    val treeEdges = remember(levelGroups) {
        val edges = mutableListOf<Pair<NetworkNode, NetworkNode>>()
        for (lvl in 2..maxLevel) {
            val children = levelGroups[lvl].orEmpty()
            val parents = levelGroups[lvl - 1].orEmpty()
            if (parents.isEmpty() || children.isEmpty()) continue
            children.forEach { child ->
                // Pick parent whose X is closest to child.X
                val parent = parents.minByOrNull { p -> kotlin.math.abs(p.position.x - child.position.x) }
                if (parent != null) edges += parent to child
            }
        }
        edges
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Network Graph Canvas with zoom and pan - for drawing connections
        val density = LocalDensity.current
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { _ ->
                        selectedNodeId = null
                        showPreview = false
                    }
                }
        ) {
            val centerX = size.width / 2
            val rootCenterY = 150f // dp space used in positioning math

            // Helper to compute a node's top-left and radius (in pixels), matching how Boxes are placed
            fun nodeTopLeftAndRadiusPx(xCenter: Float, yCenter: Float, isSelected: Boolean): Triple<Float, Float, Float> {
                val nodeSizeDp = if (isSelected) 60f else 50f
                val radiusPx = with(density) { (nodeSizeDp * scale).dp.toPx() / 2f }
                val topLeftX = ((xCenter - (nodeSizeDp / 2f)) * scale + offsetX)
                val topLeftY = ((yCenter - (nodeSizeDp / 2f)) * scale + offsetY)
                return Triple(topLeftX, topLeftY, radiusPx)
            }

            // Draw ROOT -> level 1 connections (bottom of root to top of child)
            run {
                val (rootTopLeftX, rootTopLeftY, rootRadiusPx) = nodeTopLeftAndRadiusPx(
                    xCenter = centerX,
                    yCenter = rootCenterY,
                    isSelected = false
                )
                val rootBottomX = rootTopLeftX + rootRadiusPx
                val rootBottomY = rootTopLeftY + rootRadiusPx * 2f

                nodes.filter { it.level == 1 }.forEach { node ->
                    val isHighlighted = selectedNodeId == node.id
                    val (childTopLeftX, childTopLeftY, childRadiusPx) = nodeTopLeftAndRadiusPx(
                        xCenter = node.position.x,
                        yCenter = node.position.y,
                        isSelected = selectedNodeId == node.id
                    )
                    val childTopX = childTopLeftX + childRadiusPx
                    val childTopY = childTopLeftY

                    drawLine(
                        color = if (isHighlighted) Color(0xFFFF9800) else Color.Gray.copy(alpha = 0.5f),
                        start = Offset(rootBottomX, rootBottomY),
                        end = Offset(childTopX, childTopY),
                        strokeWidth = if (isHighlighted) 12f else 8f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            // Draw computed tree edges (bottom of parent to top of child)
            treeEdges.forEach { (parent, child) ->
                val isHighlighted = selectedNodeId == parent.id || selectedNodeId == child.id
                val (srcTopLeftX, srcTopLeftY, srcRadiusPx) = nodeTopLeftAndRadiusPx(
                    xCenter = parent.position.x,
                    yCenter = parent.position.y,
                    isSelected = selectedNodeId == parent.id
                )
                val (dstTopLeftX, dstTopLeftY, dstRadiusPx) = nodeTopLeftAndRadiusPx(
                    xCenter = child.position.x,
                    yCenter = child.position.y,
                    isSelected = selectedNodeId == child.id
                )
                val srcBottomX = srcTopLeftX + srcRadiusPx
                val srcBottomY = srcTopLeftY + srcRadiusPx * 2f
                val dstTopX = dstTopLeftX + dstRadiusPx
                val dstTopY = dstTopLeftY

                drawLine(
                    color = if (isHighlighted) Color(0xFF2196F3) else Color.Gray.copy(alpha = 0.5f),
                    start = Offset(srcBottomX, srcBottomY),
                    end = Offset(dstTopX, dstTopY),
                    strokeWidth = if (isHighlighted) 12f else 8f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // Draw level labels on the left side
        // Label for "Me" at root level
        Card(
            modifier = Modifier
                .offset {
                    IntOffset(
                        (16 * scale + offsetX).toInt(),
                        ((150f - 15) * scale + offsetY).toInt()
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFF9800).copy(alpha = 0.15f)
            )
        ) {
            Text(
                text = stringResource(R.string.me_label),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF9800)
            )
        }

        // Labels for each relationship level
        levelGroups.keys.sorted().forEach { level ->
            val levelLabel = when (level) {
                1 -> stringResource(R.string.close_friends_label)
                2 -> stringResource(R.string.classmates_label)
                3 -> stringResource(R.string.batch_mates_label)
                4 -> stringResource(R.string.colleagues_label)
                5 -> stringResource(R.string.extended_network_label)
                else -> stringResource(R.string.level_format_label, level)
            }

            val levelColor = when (level) {
                1 -> Color(0xFFE91E63)
                2 -> Color(0xFF9C27B0)
                3 -> Color(0xFF3F51B5)
                4 -> Color(0xFF009688)
                5 -> Color(0xFF4CAF50)
                else -> Color(0xFF6200EE)
            }

            val yPosition = 150f + (level * 250f)

            Card(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (16 * scale + offsetX).toInt(),
                            ((yPosition - 15) * scale + offsetY).toInt()
                        )
                    },
                colors = CardDefaults.cardColors(
                    containerColor = levelColor.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = levelLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )
            }
        }

        // Draw root node (User) with profile image
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        ((screenWidth / 2 - 25) * scale + offsetX).toInt(),
                        ((150f - 25) * scale + offsetY).toInt()
                    )
                }
                .size((50 * scale).dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color(0xFFFF9800))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.you_label),
                    modifier = Modifier.size((28 * scale).dp),
                    tint = Color.White
                )
            }
        }

        // Draw contact nodes with profile images
        nodes.forEach { node ->
            val isSelected = selectedNodeId == node.id
            val isConnected = selectedNodeId?.let { selectedId ->
                val selectedNode = nodes.find { it.id == selectedId }
                selectedNode?.connections?.contains(node.id) == true || node.id == selectedId
            } ?: false

            val nodeSize = if (isSelected) 60 else 50
            val borderColor = when {
                isSelected -> Color.Blue
                isConnected -> Color.Cyan
                else -> node.color
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            ((node.position.x - nodeSize / 2) * scale + offsetX).toInt(),
                            ((node.position.y - nodeSize / 2) * scale + offsetY).toInt()
                        )
                    }
                    .size((nodeSize * scale).dp)
                    .pointerInput(node.id) {
                        detectTapGestures {
                            if (selectedNodeId == node.id) {
                                // Second tap - open full profile
                                showPreview = false
                                onNodeClick(node.id)
                            } else {
                                // First tap - show preview
                                selectedNodeId = node.id
                                showPreview = true
                            }
                        }
                    }
            ) {
                if (node.photoUrl.isNotEmpty()) {
                    // Display contact photo - parse string to Uri for local content
                    var imageLoadFailed by remember { mutableStateOf(false) }

                    if (!imageLoadFailed) {
                        AsyncImage(
                            model = node.photoUrl.toUri(),
                            contentDescription = node.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 2.dp,
                                    color = borderColor,
                                    shape = CircleShape
                                ),
                            contentScale = ContentScale.Crop,
                            onError = { imageLoadFailed = true }
                        )
                    } else {
                        // Fallback to default icon when image fails to load
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(node.color.copy(alpha = 0.7f))
                                .border(
                                    width = if (isSelected) 3.dp else 2.dp,
                                    color = borderColor,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = node.name,
                                modifier = Modifier.size((nodeSize * 0.55f).dp),
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    // Display default profile icon
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(node.color.copy(alpha = 0.7f))
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = borderColor,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = node.name,
                            modifier = Modifier.size((nodeSize * 0.55f).dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Zoom indicator
        Card(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
            )
        ) {
            Text(
                text = "Zoom: ${String.format(Locale.US, "%.1f", scale)}x",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Contact Preview Card
        if (showPreview && selectedNodeId != null) {
            val selectedNode = nodes.find { it.id == selectedNodeId }
            selectedNode?.let { node ->
                // Get relationship level info
                val levelLabel = when (node.level) {
                    1 -> "Close Friends"
                    2 -> "Classmates"
                    3 -> "Batch Mates"
                    4 -> "Colleagues"
                    5 -> "Extended Network"
                    else -> "Level ${node.level}"
                }

                // Count contacts at the same level
                val sameLevelCount = nodes.count { it.level == node.level }

                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    onClick = {
                        showPreview = false
                        onNodeClick(node.id)
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = node.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = levelLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = node.color,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$sameLevelCount ${if (sameLevelCount == 1) "contact" else "contacts"} at this level",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to view full profile",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Empty state when no contacts
        if (nodes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No Contacts Yet",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Add your first contact to start building your network",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pinch to zoom • Drag to pan",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        // User info overlay with legend
//        currentUserEmail?.let {
//            Card(
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .padding(16.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
//                )
//            ) {
//                Column(modifier = Modifier.padding(12.dp)) {
//                    Text(
//                        text = "Your Network Tree",
//                        style = MaterialTheme.typography.labelLarge,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    // Level legend
//                    LevelIndicator(1, "Close Friends", Color(0xFFE91E63))
//                    LevelIndicator(2, "Classmates", Color(0xFF9C27B0))
//                    LevelIndicator(3, "Batch Mates", Color(0xFF3F51B5))
//                    LevelIndicator(4, "Colleagues", Color(0xFF009688))
//                    LevelIndicator(5, "Extended", Color(0xFF4CAF50))
//                }
//            }
//        }
    }
}

@Composable
fun LevelIndicator(level: Int, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "L$level: $label",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp
        )
    }
}

// Generate nodes from contacts in a vertical tree structure
private fun generateNodesFromContacts(contacts: List<Contact>): List<NetworkNode> {
    if (contacts.isEmpty()) return emptyList()

    val nodes = mutableListOf<NetworkNode>()

    val baseX = 540f // Center horizontally
    val startY = 150f // Start from top
    val levelSpacing = 250f // Vertical spacing between levels

    // Define level colors based on relationship category
    val levelColors = mapOf(
        1 to Color(0xFFE91E63), // Pink - Close Friends
        2 to Color(0xFF9C27B0), // Purple - Classmates
        3 to Color(0xFF3F51B5), // Blue - Batch Mates
        4 to Color(0xFF009688), // Teal - Colleagues
        5 to Color(0xFF4CAF50)  // Green - Extended Network
    )

    val contactsByLevel = contacts.groupBy { it.relationshipLevel }

    // Calculate positions for each level in tree structure
    contactsByLevel.toSortedMap().forEach { (level, contactsInLevel) ->
        val y = startY + (level * levelSpacing) // Move down for each level
        val nodeSpacing = 200f // Horizontal spacing between nodes
        val totalWidth = (contactsInLevel.size - 1) * nodeSpacing
        val startX = baseX - (totalWidth / 2) // Center the nodes horizontally

        contactsInLevel.forEachIndexed { index, contact ->
            val x = startX + (index * nodeSpacing)

            nodes.add(
                NetworkNode(
                    id = contact.id,
                    name = contact.name,
                    position = Offset(x, y),
                    photoUrl = contact.photoUrl,
                    connections = contact.connectedTo,
                    color = levelColors[level] ?: Color(0xFF6200EE),
                    level = level
                )
            )
        }
    }

    return nodes
}
