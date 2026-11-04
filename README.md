<h1 style="text-align: center;">💬 Bubblify &mdash; Immersive Chat Bubbles</h1>
<h3 style="text-align: center;"><strong>Bring conversations into the world.</strong></h3>
<p style="text-align: center;"><strong>Bubblify</strong> transforms how players communicate in Minecraft by displaying chat messages in floating speech bubbles above their characters.</p>
<p style="text-align: center;">Originally designed for survival servers and roleplay communities, the mod brings attention back to the world around you instead of keeping every conversation in the corner of the screen.</p>
<p style="text-align: center;">Whether you are gathering around a campfire, exploring with friends, or taking part in a narrative event, Bubblify gives conversations a visual presence. 🏕️</p>
<p style="text-align: center;"><img src="https://media.forgecdn.net/attachments/description/1484286/description_1115d27d-8794-4aa9-b7b4-69533d66dfc4.png"></p>
<p style="text-align: center;">&nbsp;</p>
<h2 style="text-align: center;"><strong>⊰═══════════════════════════════════════════════⊱</strong></h2>
<h2 style="text-align: left;"><strong>✨ Key Features</strong></h2>
<ul>
<li style="text-align: left;">💬 In-World Speech Bubbles &mdash; Messages appear above players and follow their movement, making it easier to identify who is speaking.</li>
<li style="text-align: left;">🎨 RGB Background Customization &mdash; Personalize the default bubble&rsquo;s background using the in-game RGB color picker.</li>
<li style="text-align: left;">🖤 Black or White Text &mdash; Choose the text color that best contrasts with your bubble design.</li>
<li style="text-align: left;">🖼️ Custom Bubble Styles &mdash; Create designs using separate textures for the upper decoration, message panel, and tail.</li>
<li style="text-align: left;">📚 Organized Conversations &mdash; Messages support stacking and automatic line wrapping.</li>
<li style="text-align: left;">💾 Saved Color Preferences &mdash; Your selected color is saved locally, so you do not need to configure it every time you play.</li>
<li style="text-align: left;">🎭 Narrative Roles &mdash; Support roleplay events with designated NPC and Children roles.</li>
<li style="text-align: left;">🔐 Style Permissions &mdash; Control access to custom bubble styles through the server&rsquo;s permission system.</li>
</ul>
<h2 style="text-align: center;">&nbsp;</h2>
<h2 style="text-align: center;"><strong>⊰═══════════════════════════════════════════════⊱</strong></h2>
<p>&nbsp;</p>
<h2><strong>🎭 Built for Roleplay and Immersion</strong></h2>
<p style="text-align: center;">Give your characters a voice that feels connected to the scene.</p>
<p style="text-align: center;">Bubblify complements town conversations, quest interactions, community events, and story-driven adventures by helping players see who is speaking.</p>
<p style="text-align: center;">Customize your bubble designs to match your server&rsquo;s characters, atmosphere, and visual identity. ✨</p>
<p style="text-align: center;">&nbsp;</p>
<h2 style="text-align: center;"><strong>⊰═══════════════════════════════════════════════⊱</strong></h2>
<p>&nbsp;</p>
<h2><strong>📁 Setting Up Custom Bubble Styles</strong></h2>
<p>Custom styles are organized inside your server&rsquo;s <code>config</code> folder.</p>
<h3><strong>1️⃣ Create the Main Folder</strong></h3>
<p>Open your server&rsquo;s files, locate the <code>config</code> folder, and create a folder named <code>bubblifymod</code>.</p>
<p>Inside it, create:</p>
<ul>
<li>A file named <strong><code>config.json</code></strong>.</li>
<li>A folder named <strong><code>styles</code></strong>.</li>
</ul>
<p>Your structure should look like this:</p>
<pre><code>config/
└── bubblifymod/
    ├── config.json
    └── styles/</code></pre>
<h3><strong>2️⃣ Configure config.json</strong></h3>
<p>Open <strong><code>config.json</code></strong> and insert:</p>
<pre><code>{
  "defaultStyleId": "default"
}</code></pre>
<p>This example keeps <code>default</code> as the configured default style ID.</p>
<h3><strong>3️⃣ Create Your Style Folder</strong></h3>
<p>Inside <code>styles</code>, create a separate folder for each custom bubble.</p>
<p>For this tutorial, the example style is named <strong><code>children_demon</code></strong>:</p>
<pre><code>config/
└── bubblifymod/
    ├── config.json
    └── styles/
        └── children_demon/
            ├── style.json
            ├── superior.png
            ├── painel.png
            └── cauda.png</code></pre>
<p>📌 Each custom style should have its own folder, configuration file, and textures.</p>
<p>📄 Make sure the configuration is saved as <code>style.json</code>, not <code>style.json.txt</code>.</p>
<h2 style="text-align: center;">&nbsp;</h2>
<h2 style="text-align: center;"><strong>⊰═══════════════════════════════════════════════⊱</strong></h2>
<p>&nbsp;</p>
<h2 style="text-align: center;"><strong>🖼️ Creating the PNG Textures</strong></h2>
<p>Each style uses three PNG images:</p>
<ul>
<li>👑 <strong><code>superior.png</code> </strong>&mdash; the upper decoration.</li>
<li>💬 <strong><code>painel.png</code></strong> &mdash; the main background behind the message.</li>
<li>🔻 <code><strong>cauda.png</strong></code> &mdash; the speech bubble&rsquo;s tail.</li>
</ul>
<h3><strong>📐 Example Image Sizes</strong></h3>
<p>For a first design, you can use:</p>
<pre><code>superior.png &rarr; 80 &times; 16 pixels
painel.png   &rarr; 128 &times; 42 pixels
cauda.png    &rarr; 16 &times; 8 pixels</code></pre>
<p style="text-align: center;"><img style="display: block; margin-left: auto; margin-right: auto;" src="https://media.forgecdn.net/attachments/description/1484286/description_7789bb97-4830-4a67-8808-8eec64eae119.png"> <img style="display: block; margin-left: auto; margin-right: auto;" src="https://media.forgecdn.net/attachments/description/1484286/description_6fb5918d-7232-492a-83a0-218383ee8404.png"><img style="display: block; margin-left: auto; margin-right: auto;" src="https://media.forgecdn.net/attachments/description/1484286/description_b3b301fe-5e4f-4cab-949f-4ee614b0a4aa.png"></p>
<h3><strong>🎨 Texture Tips</strong></h3>
<p>&nbsp;</p>
<ul>
<li>Use PNG transparency around decorative shapes where needed.</li>
<li>Keep enough space inside the panel for readable text.</li>
<li>Match the filenames in<strong> <code>style.json</code></strong> to the actual image filenames.</li>
<li>Set <code>sourceWidth</code> to the actual width of <strong><code>painel.png</code></strong>.</li>
<li>The top and tail dimensions describe their cropped areas. Extra transparent margins can make the complete PNG larger than these areas.</li>
</ul>
<p>💡 For your first style, use the example dimensions without extra transparent margins on the top and tail.</p>
<h2 style="text-align: center;">&nbsp;</h2>
<h2 style="text-align: center;"><strong>⊰═══════════════════════════════════════════════⊱</strong></h2>
<p>&nbsp;</p>
<h2><strong>📝 Creating style.json</strong></h2>
<p>Inside your style folder, create <strong><code>style.json</code></strong> and paste the following:</p>
<pre><code>{
  "id": "children_demon",
  "displayName": "Demon",
  "category": "CHILDREN",

  "top": "superior.png",
  "body": "painel.png",
  "tail": "cauda.png",

  "sourceWidth": 128,
  "bodyHeight": 42,

  "panelAspectRatio": 3.0,
  "maxTextWidth": 64,

  "topWidth": 80,
  "topHeight": 16,
  "topAlign": 0.5,
  "topOffsetX": 0,
  "topInset": 7,

  "tailWidth": 16,
  "tailHeight": 8,
  "tailAlign": 0.5,
  "tailOffsetX": 0,
  "tailInset": 4,

  "paddingLeft": 16,
  "paddingRight": 16,
  "paddingTop": 7,
  "paddingBottom": 7,

  "heightOffset": 2.1,
  "fullBright": true,

  "stackMode": "shared",
  "panelGap": 0,
  "uniformPanelWidth": true
}</code></pre>
<h2 style="text-align: center;">&nbsp;</h2>
<h2 style="text-align: center;"><strong>⊰═══════════════════════════════════════════════⊱</strong></h2>
<p>&nbsp;</p>
<h2><strong>⚙️ Understanding the Style Configuration</strong></h2>
<h3><strong>🏷️ Style Identity</strong></h3>
<ul>
<li><code>id</code> &mdash; internal identifier for the style. Give each style a distinct ID.</li>
<li><code>displayName</code> &mdash; name assigned to the style for display.</li>
<li><code>category</code> &mdash; category assigned to the style. This example uses <code>CHILDREN</code>.</li>
</ul>
<h3>🧩 Texture Filenames</h3>
<ul>
<li><code>top</code> &mdash; upper decoration image.</li>
<li><code>body</code> &mdash; main message panel image.</li>
<li><code>tail</code> &mdash; speech bubble tail image.</li>
</ul>
<p>The filenames must match the PNG files inside the style folder.</p>
<h3><strong>📏 Main Panel Settings</strong></h3>
<ul>
<li><code>sourceWidth</code> &mdash; original width of the body texture, in pixels. It must match the actual width of <code>painel.png</code>.</li>
<li><code>bodyHeight</code> &mdash; usable panel height before the tail, in pixels.</li>
<li><code>panelAspectRatio</code> &mdash; intended width-to-height ratio; not currently applied.</li>
<li><code>maxTextWidth</code> &mdash; intended text width before wrapping; not currently applied.</li>
</ul>
<h3><strong>👑 Upper Decoration Settings</strong></h3>
<ul>
<li><code>topWidth</code> &mdash; width of the cropped upper decoration.</li>
<li><code>topHeight</code> &mdash; height of the cropped upper decoration.</li>
<li><code>topAlign</code> &mdash; horizontal alignment.</li>
<li><code>topOffsetX</code> &mdash; additional horizontal adjustment.</li>
<li><code>topInset</code> &mdash; inset adjustment for the upper decoration, in pixels.</li>
</ul>
<p>Alignment values:</p>
<pre><code>0.0 &rarr; Left
0.5 &rarr; Center
1.0 &rarr; Right</code></pre>
<p>For <code>topOffsetX</code>, negative values move the decoration left and positive values move it right.</p>
<h3><strong>🔻 Tail Settings</strong></h3>
<ul>
<li><code>tailWidth</code> &mdash; width of the cropped tail.</li>
<li><code>tailHeight</code> &mdash; height of the cropped tail.</li>
<li><code>tailAlign</code> &mdash; horizontal alignment, using the same values as <code>topAlign</code>.</li>
<li><code>tailOffsetX</code> &mdash; additional horizontal adjustment.</li>
<li><code>tailInset</code> &mdash; inset adjustment for the tail, in pixels.</li>
</ul>
<h3><strong>📝 Text Padding</strong></h3>
<p>These settings define the space between the panel&rsquo;s edges and the message:</p>
<ul>
<li><code>paddingLeft</code></li>
<li><code>paddingRight</code></li>
<li><code>paddingTop</code></li>
<li><code>paddingBottom</code></li>
</ul>
<p>Increase the corresponding padding when your artwork needs more room around the text.</p>
<h3><strong>💡 Height and Lighting</strong></h3>
<ul>
<li><code>heightOffset</code> &mdash; fine height adjustment. Accepts positive or negative values.</li>
<li><code>fullBright</code> &mdash; <code>true</code> keeps the text fully bright; <code>false</code> uses normal lighting.</li>
</ul>
<h3><strong>📚 Message Stacking</strong></h3>
<ul>
<li><code>stackMode</code> &mdash; <code>"shared"</code> uses a shared panel for consecutive messages.</li>
<li><code>panelGap</code> &mdash; space between stacked panels, in pixels.</li>
<li><code>uniformPanelWidth</code> &mdash; when <code>true</code>, stacked messages use the largest width in the stack.</li>
</ul>
<h2 style="text-align: center;">&nbsp;</h2>
<h2 style="text-align: center;"><strong>⊰═══════════════════════════════════════════════⊱</strong></h2>
<h2>&nbsp;</h2>
<h2><strong>🎨 Adding More Styles</strong></h2>
<p>To create another bubble design:</p>
<ol>
<li>Create another folder inside <code>styles</code>.</li>
<li>Add its three PNG textures.</li>
<li>Add its own <code>style.json</code>.</li>
<li>Give it a distinct <code>id</code>.</li>
<li>Adjust the filenames, dimensions, and layout settings to match the artwork.</li>
</ol>
<p>Example:</p>
<pre><code>config/
└── bubblifymod/
    ├── config.json
    └── styles/
        ├── children_demon/
        │   ├── style.json
        │   ├── superior.png
        │   ├── painel.png
        │   └── cauda.png
        │
        └── another_style/
            ├── style.json
            ├── superior.png
            ├── painel.png
            └── cauda.png</code></pre>
<p>🔐 Creating a style does not automatically grant every player access to it. Style availability also depends on the server&rsquo;s permission system.</p>
<h2 style="text-align: center;">&nbsp;</h2>
<h2 style="text-align: center;"><strong>⊰═══════════════════════════════════════════════⊱</strong></h2>
<p>&nbsp;</p>
<h2><strong>🛠️ Server Administration</strong></h2>
<p>Administrative commands require permission level 2 or higher.</p>
<h3><strong>💬 Chat Setting</strong></h3>
<p>Disable the chat setting:</p>
<pre><code>/bubblify show_chat false</code></pre>
<p>Enable it again:</p>
<pre><code>/bubblify show_chat true</code></pre>
<h3><strong><img style="display: block; margin-left: auto; margin-right: auto;" src="https://media.forgecdn.net/attachments/description/1484286/description_1efddf92-f54f-493b-894b-f268477456e8.png"></strong></h3>
<h3>&nbsp;</h3>
<h3><strong>🎭 Narrative Roles</strong></h3>
<p>Assign the NPC role:</p>
<pre><code>/bubblify class &lt;player&gt; npc</code></pre>
<p>Assign the Children role:</p>
<pre><code>/bubblify class &lt;player&gt; children</code></pre>
<p>Remove a narrative role:</p>
<pre><code>/bubblify class &lt;player&gt; clear</code></pre>
<p>Replace <code>&lt;player&gt;</code> with the target player&rsquo;s name.</p>
<p><img style="display: block; margin-left: auto; margin-right: auto;" src="https://media.forgecdn.net/attachments/description/1484286/description_55bedfb4-a66e-448a-ad84-77590a2e8ac9.png"></p>
<h2 style="text-align: center;">&nbsp;</h2>
<h2 style="text-align: center;"><strong>⊰═══════════════════════════════════════════════⊱</strong></h2>
<p>&nbsp;</p>
<h2><strong>📦 Installation</strong></h2>
<p>Bubblify is required on both the client and the server for its multiplayer features.</p>
<ul>
<li>🖥️ Client &mdash; Renders speech bubbles and provides the customization interface.</li>
<li>🌐 Server &mdash; Synchronizes multiplayer information and manages server-side settings.</li>
</ul>
<p>Install the matching release on both sides, and check the supported Minecraft and Forge versions listed for the download.</p>
<p>&nbsp;</p>
<h2 style="text-align: center;"><strong>⊰═══════════════════════════════════════════════⊱</strong></h2>
<h3 style="text-align: center;">&nbsp;</h3>
<h3 style="text-align: center;"><strong>💬 Every character has a voice. Give yours a bubble.</strong></h3>
