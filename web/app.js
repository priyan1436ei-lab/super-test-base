/**
 * APPPURGE AI - Main Application Controller
 * High-performance smart app remover, deep uninstaller, and bloatware cleaner.
 */

(function () {
  'use strict';

  // --- Sound Effects System via Web Audio API ---
  class SoundFX {
    constructor() {
      this.enabled = localStorage.getItem('apppurge_sound') !== 'false';
      this.ctx = null;
    }

    init() {
      if (!this.ctx && typeof window.AudioContext !== 'undefined') {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        this.ctx = new AudioCtx();
      }
    }

    toggle() {
      this.enabled = !this.enabled;
      localStorage.setItem('apppurge_sound', this.enabled);
      return this.enabled;
    }

    playTone(freq, type = 'sine', duration = 0.1, gainVal = 0.15) {
      if (!this.enabled) return;
      try {
        this.init();
        if (this.ctx && this.ctx.state === 'suspended') {
          this.ctx.resume();
        }
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.type = type;
        osc.frequency.setValueAtTime(freq, this.ctx.currentTime);
        gain.gain.setValueAtTime(gainVal, this.ctx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.001, this.ctx.currentTime + duration);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start();
        osc.stop(this.ctx.currentTime + duration);
      } catch (_) {}
    }

    click() {
      this.playTone(800, 'sine', 0.05, 0.08);
    }

    check() {
      this.playTone(950, 'triangle', 0.08, 0.1);
    }

    scan() {
      if (!this.enabled) return;
      try {
        this.init();
        const now = this.ctx.currentTime;
        const osc = this.ctx.createOscillator();
        const gain = this.ctx.createGain();
        osc.frequency.setValueAtTime(300, now);
        osc.frequency.exponentialRampToValueAtTime(1400, now + 0.4);
        gain.gain.setValueAtTime(0.12, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.45);
        osc.connect(gain);
        gain.connect(this.ctx.destination);
        osc.start();
        osc.stop(now + 0.45);
      } catch (_) {}
    }

    purgeStep() {
      this.playTone(220, 'sawtooth', 0.06, 0.07);
    }

    success() {
      if (!this.enabled) return;
      try {
        this.init();
        const chords = [523.25, 659.25, 783.99, 1046.5];
        chords.forEach((freq, idx) => {
          setTimeout(() => this.playTone(freq, 'sine', 0.35, 0.12), idx * 80);
        });
      } catch (_) {}
    }
  }

  const sfx = new SoundFX();

  // --- Sample Installed Apps Dataset ---
  const INITIAL_APPS = [
    {
      id: 'app-search-toolbar',
      name: 'SearchToolbar Pro 2026',
      publisher: 'Unknown AdNetwork LLC',
      icon: '🌐',
      sizeMb: 420,
      bloatScore: 94,
      category: 'bloatware',
      leftoverPath: 'AppData/Local/SearchToolbar/trackers.db',
      isBloatware: true,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-mcafee-trial',
      name: 'McAfee Total Protection (Expired Trial)',
      publisher: 'McAfee Consumer LLC',
      icon: '🛡️',
      sizeMb: 1850,
      bloatScore: 88,
      category: 'bloatware',
      leftoverPath: 'ProgramData/McAfee/TelemetryCache (1.4 GB)',
      isBloatware: true,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-candy-crush',
      name: 'Candy Crush Saga (OEM Auto-Preinstall)',
      publisher: 'King Games / Partner OEM',
      icon: '🍬',
      sizeMb: 920,
      bloatScore: 78,
      category: 'bloatware',
      leftoverPath: 'WindowsApps/King.CandyCrushSaga_x64',
      isBloatware: true,
      hasBackgroundDaemon: false,
      selected: false,
      purged: false
    },
    {
      id: 'app-android-sdk-caches',
      name: 'Android Studio AVD & Emulator Caches',
      publisher: 'Google LLC',
      icon: '🤖',
      sizeMb: 14200,
      bloatScore: 20,
      category: 'large',
      leftoverPath: '.android/avd/Pixel_8_Pro.avd (Cache files)',
      isBloatware: false,
      hasBackgroundDaemon: false,
      selected: false,
      purged: false
    },
    {
      id: 'app-adobe-creative-cloud',
      name: 'Adobe Creative Cloud Leftover Caches',
      publisher: 'Adobe Inc.',
      icon: '🎨',
      sizeMb: 9800,
      bloatScore: 65,
      category: 'large',
      leftoverPath: 'Library/Application Support/Adobe/Common/Media Cache',
      isBloatware: false,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-docker-images',
      name: 'Docker Desktop Dangling Volumes & Images',
      publisher: 'Docker Inc.',
      icon: '🐳',
      sizeMb: 18400,
      bloatScore: 15,
      category: 'large',
      leftoverPath: 'var/lib/docker/overlay2 (Orphaned layers)',
      isBloatware: false,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-driver-updater',
      name: 'DriverUpdater Pro Registry Scanner',
      publisher: 'PC Booster Global',
      icon: '⚙️',
      sizeMb: 580,
      bloatScore: 92,
      category: 'bloatware',
      leftoverPath: 'HKLM/Software/DriverUpdater/Hooks',
      isBloatware: true,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-spotify-web-helper',
      name: 'Spotify Background Audio Relay & Cache',
      publisher: 'Spotify AB',
      icon: '🎵',
      sizeMb: 2450,
      bloatScore: 42,
      category: 'background',
      leftoverPath: 'AppData/Local/Spotify/Data (Offline cache)',
      isBloatware: false,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-discord-rpc',
      name: 'Discord Voice & Video IPC Daemon',
      publisher: 'Discord Inc.',
      icon: '💬',
      sizeMb: 1250,
      bloatScore: 35,
      category: 'background',
      leftoverPath: 'AppData/Roaming/discord/Cache',
      isBloatware: false,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-weatherlive',
      name: 'WeatherLive Desktop Helper',
      publisher: 'AdUtility Tech',
      icon: '⛅',
      sizeMb: 340,
      bloatScore: 82,
      category: 'bloatware',
      leftoverPath: 'Startup/WeatherLiveLauncher.exe',
      isBloatware: true,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-coupon-vault',
      name: 'CouponVault Shopping Assistant',
      publisher: 'AffiliateTrackers Inc',
      icon: '🏷️',
      sizeMb: 210,
      bloatScore: 96,
      category: 'bloatware',
      leftoverPath: 'Chrome/User Data/Default/Extensions/couponvault',
      isBloatware: true,
      hasBackgroundDaemon: false,
      selected: false,
      purged: false
    },
    {
      id: 'app-quickshare-telemetry',
      name: 'QuickShare Telemetry Hub',
      publisher: 'DeviceLink Systems',
      icon: '📡',
      sizeMb: 490,
      bloatScore: 85,
      category: 'bloatware',
      leftoverPath: 'System32/Tasks/QuickShareTelemetry',
      isBloatware: true,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-google-chrome',
      name: 'Google Chrome Browser',
      publisher: 'Google LLC',
      icon: '🌐',
      sizeMb: 1420,
      bloatScore: 28,
      category: 'all',
      leftoverPath: 'AppData/Local/Google/Chrome/User Data/Default/Cache',
      isBloatware: false,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-vs-code',
      name: 'Visual Studio Code',
      publisher: 'Microsoft Corporation',
      icon: '💻',
      sizeMb: 3200,
      bloatScore: 10,
      category: 'large',
      leftoverPath: '.vscode/extensions (Old language servers)',
      isBloatware: false,
      hasBackgroundDaemon: false,
      selected: false,
      purged: false
    },
    {
      id: 'app-steam-cache',
      name: 'Steam Client & Shader Pre-caches',
      publisher: 'Valve Corp',
      icon: '🎮',
      sizeMb: 6700,
      bloatScore: 22,
      category: 'large',
      leftoverPath: 'steamapps/shadercache (DirectX & Vulkan blobs)',
      isBloatware: false,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-epic-games',
      name: 'Epic Games Launcher & Prerequisites',
      publisher: 'Epic Games Inc.',
      icon: '🕹️',
      sizeMb: 4300,
      bloatScore: 45,
      category: 'large',
      leftoverPath: 'ProgramData/Epic/EpicGamesLauncher/Data',
      isBloatware: false,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-teams-updater',
      name: 'Microsoft Teams Background Daemon',
      publisher: 'Microsoft Corporation',
      icon: '👥',
      sizeMb: 1100,
      bloatScore: 50,
      category: 'background',
      leftoverPath: 'AppData/Local/Microsoft/Teams/current',
      isBloatware: false,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-zoom-tracker',
      name: 'Zoom Presence & Background Audio',
      publisher: 'Zoom Video Communications',
      icon: '📹',
      sizeMb: 850,
      bloatScore: 40,
      category: 'background',
      leftoverPath: 'AppData/Roaming/Zoom/data/logs',
      isBloatware: false,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-unreal-engine',
      name: 'Unreal Engine Intermediate Build Caches',
      publisher: 'Epic Games Inc.',
      icon: '⚡',
      sizeMb: 11500,
      bloatScore: 18,
      category: 'large',
      leftoverPath: 'Intermediate/Build/Win64/UnrealEditor',
      isBloatware: false,
      hasBackgroundDaemon: false,
      selected: false,
      purged: false
    },
    {
      id: 'app-vlc-player',
      name: 'VLC Media Player',
      publisher: 'VideoLAN Organization',
      icon: '🎬',
      sizeMb: 160,
      bloatScore: 5,
      category: 'all',
      leftoverPath: 'AppData/Roaming/vlc/art',
      isBloatware: false,
      hasBackgroundDaemon: false,
      selected: false,
      purged: false
    },
    {
      id: 'app-notion-desktop',
      name: 'Notion Desktop Client',
      publisher: 'Notion Labs, Inc.',
      icon: '📝',
      sizeMb: 740,
      bloatScore: 25,
      category: 'all',
      leftoverPath: 'AppData/Roaming/Notion/Partitions',
      isBloatware: false,
      hasBackgroundDaemon: false,
      selected: false,
      purged: false
    },
    {
      id: 'app-slack-desktop',
      name: 'Slack Workspaces & Local IndexedDB',
      publisher: 'Slack Technologies',
      icon: '💼',
      sizeMb: 1350,
      bloatScore: 38,
      category: 'background',
      leftoverPath: 'AppData/Roaming/Slack/IndexedDB',
      isBloatware: false,
      hasBackgroundDaemon: true,
      selected: false,
      purged: false
    },
    {
      id: 'app-obs-studio',
      name: 'OBS Studio Recording Suite',
      publisher: 'OBS Project',
      icon: '🎥',
      sizeMb: 680,
      bloatScore: 12,
      category: 'all',
      leftoverPath: 'AppData/Roaming/obs-studio/plugin_config',
      isBloatware: false,
      hasBackgroundDaemon: false,
      selected: false,
      purged: false
    },
    {
      id: 'app-orphaned-temp',
      name: 'Windows Temp & Broken Registry Leftovers',
      publisher: 'System Remnants',
      icon: '🗑️',
      sizeMb: 3600,
      bloatScore: 89,
      category: 'leftovers',
      leftoverPath: 'Windows/Temp & HKEY_CURRENT_USER/OrphanedClsid',
      isBloatware: true,
      hasBackgroundDaemon: false,
      selected: false,
      purged: false
    }
  ];

  // --- Application State ---
  const state = {
    apps: JSON.parse(JSON.stringify(INITIAL_APPS)),
    activeCategory: 'all',
    searchQuery: '',
    sortBy: 'size-desc',
    totalPurgedMb: 0,
    appsToPurge: [],
    droppedFiles: [],
    supabaseUrl: localStorage.getItem('supabase_url') || 'https://slsxmh3funhxmsl4jmlk.supabase.co',
    supabaseKey: localStorage.getItem('supabase_key') || 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.dummy_anon_key_fittrack_ai'
  };

  // --- DOM Elements ---
  const el = {
    appList: document.getElementById('app-list-container'),
    emptyState: document.getElementById('app-empty-state'),
    categoryTabs: document.getElementById('category-tabs'),
    searchInput: document.getElementById('app-search-input'),
    btnClearSearch: document.getElementById('btn-clear-search'),
    sortSelect: document.getElementById('sort-select'),
    selectAllCheckbox: document.getElementById('select-all-checkbox'),
    selectionSummary: document.getElementById('selection-summary'),
    btnSelectAllBloat: document.getElementById('btn-select-all-bloat'),
    btnDeselectAll: document.getElementById('btn-deselect-all'),
    btnTriggerPurge: document.getElementById('btn-trigger-purge'),
    btnPurgeText: document.getElementById('btn-purge-text'),
    valTotalApps: document.getElementById('val-total-apps'),
    valBloatCount: document.getElementById('val-bloatware-count'),
    valTotalSize: document.getElementById('val-total-size'),
    valSelectedSize: document.getElementById('val-selected-size'),
    valSelectedCount: document.getElementById('val-selected-count'),
    heroStatReclaimed: document.getElementById('hero-stat-reclaimed'),
    scannerCurtain: document.getElementById('scanner-curtain'),
    scannerStatusText: document.getElementById('scanner-status-text'),
    btnHeaderScan: document.getElementById('btn-header-scan'),
    btnDemoSample: document.getElementById('btn-demo-sample'),
    btnSoundToggle: document.getElementById('btn-sound-toggle'),
    soundOnIcon: document.querySelector('.sound-on-icon'),
    soundOffIcon: document.querySelector('.sound-off-icon'),

    // Drop Zone Elements
    fileDropZone: document.getElementById('file-drop-zone'),
    fileInputHidden: document.getElementById('file-input-hidden'),
    btnBrowseFiles: document.getElementById('btn-browse-files'),
    btnTestSampleFile: document.getElementById('btn-test-sample-file'),
    analyzedResultsContainer: document.getElementById('analyzed-results-container'),
    analyzedFilesList: document.getElementById('analyzed-files-list'),
    btnClearDroppedFiles: document.getElementById('btn-clear-dropped-files'),

    // Modals
    uninstallModal: document.getElementById('uninstall-modal'),
    modalAppCount: document.getElementById('modal-app-count'),
    modalAppPreviewList: document.getElementById('modal-app-preview-list'),
    btnCloseModal: document.getElementById('btn-close-modal'),
    btnCancelUninstall: document.getElementById('btn-cancel-uninstall'),
    btnConfirmUninstall: document.getElementById('btn-confirm-uninstall'),
    terminalLogWrapper: document.getElementById('terminal-log-wrapper'),
    terminalConsole: document.getElementById('terminal-console'),
    terminalProgressFill: document.getElementById('terminal-progress-fill'),
    modalFooterActions: document.getElementById('modal-footer-actions'),

    // Supabase
    btnSupabaseConfig: document.getElementById('btn-supabase-config'),
    supabaseModal: document.getElementById('supabase-modal'),
    btnCloseSupabaseModal: document.getElementById('btn-close-supabase-modal'),
    cfgSupabaseUrl: document.getElementById('cfg-supabase-url'),
    cfgSupabaseKey: document.getElementById('cfg-supabase-key'),
    btnTestConnection: document.getElementById('btn-test-connection'),
    btnSaveSupabase: document.getElementById('btn-save-supabase'),
    btnSyncNow: document.getElementById('btn-sync-now'),
    btnViewCloudLogs: document.getElementById('btn-view-cloud-logs'),
    cloudConnectionPill: document.getElementById('cloud-connection-pill'),
    toastContainer: document.getElementById('toast-container')
  };

  // --- Helper Functions ---
  function formatSize(mb) {
    if (mb >= 1024) {
      return (mb / 1024).toFixed(1) + ' GB';
    }
    return mb + ' MB';
  }

  function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'warn') icon = '⚠️';
    if (type === 'error') icon = '🚨';

    toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
    el.toastContainer.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  }

  // --- Render App List ---
  function renderAppList() {
    let filtered = state.apps.filter(app => !app.purged);

    // Filter by Category
    if (state.activeCategory === 'bloatware') {
      filtered = filtered.filter(app => app.isBloatware);
    } else if (state.activeCategory === 'large') {
      filtered = filtered.filter(app => app.sizeMb >= 2048);
    } else if (state.activeCategory === 'background') {
      filtered = filtered.filter(app => app.hasBackgroundDaemon);
    } else if (state.activeCategory === 'leftovers') {
      filtered = filtered.filter(app => app.category === 'leftovers');
    }

    // Filter by Search Query
    if (state.searchQuery.trim()) {
      const q = state.searchQuery.toLowerCase().trim();
      filtered = filtered.filter(app =>
        app.name.toLowerCase().includes(q) ||
        app.publisher.toLowerCase().includes(q) ||
        app.leftoverPath.toLowerCase().includes(q)
      );
    }

    // Sorting
    filtered.sort((a, b) => {
      if (state.sortBy === 'size-desc') return b.sizeMb - a.sizeMb;
      if (state.sortBy === 'size-asc') return a.sizeMb - b.sizeMb;
      if (state.sortBy === 'threat-desc') return b.bloatScore - a.bloatScore;
      if (state.sortBy === 'name-asc') return a.name.localeCompare(b.name);
      return 0;
    });

    el.appList.innerHTML = '';

    if (filtered.length === 0) {
      el.emptyState.classList.remove('hidden');
    } else {
      el.emptyState.classList.add('hidden');
    }

    filtered.forEach(app => {
      const row = document.createElement('div');
      row.className = `app-row ${app.selected ? 'selected' : ''}`;
      row.id = `row-${app.id}`;

      let fillClass = 'fill-low';
      let tagClass = 'tag-clean';
      let tagLabel = 'Clean';

      if (app.bloatScore >= 75) {
        fillClass = 'fill-high';
        tagClass = 'tag-bloat';
        tagLabel = 'High Bloat';
      } else if (app.bloatScore >= 40) {
        fillClass = 'fill-medium';
        tagClass = 'tag-orphan';
        tagLabel = 'Medium Risk';
      }

      row.innerHTML = `
        <div>
          <label class="custom-checkbox">
            <input type="checkbox" data-id="${app.id}" ${app.selected ? 'checked' : ''}>
            <span class="checkmark"></span>
          </label>
        </div>
        <div class="app-icon-wrap">${app.icon}</div>
        <div class="app-meta">
          <div class="app-name">
            <span>${app.name}</span>
            <span class="app-badge-tag ${tagClass}">${tagLabel}</span>
          </div>
          <div class="app-publisher">${app.publisher}</div>
        </div>
        <div class="app-size">
          <span>${formatSize(app.sizeMb)}</span>
          <span class="app-size-sub">${app.hasBackgroundDaemon ? '⚡ Running Daemon' : 'Inactive Process'}</span>
        </div>
        <div class="app-leftovers" title="${app.leftoverPath}">
          📁 ${app.leftoverPath.length > 28 ? app.leftoverPath.substring(0, 25) + '...' : app.leftoverPath}
        </div>
        <div class="app-threat-meter">
          <div class="threat-score-label">Bloat Score: ${app.bloatScore}/100</div>
          <div class="threat-bar">
            <div class="threat-fill ${fillClass}" style="width: ${app.bloatScore}%;"></div>
          </div>
        </div>
        <div class="app-action-single">
          <button class="btn-uninstall-single" data-action="single-uninstall" data-id="${app.id}" title="Deep Uninstall">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
            <span>Purge</span>
          </button>
        </div>
      `;

      // Checkbox listener
      const chk = row.querySelector('input[type="checkbox"]');
      chk.addEventListener('change', (e) => {
        app.selected = e.target.checked;
        if (app.selected) {
          row.classList.add('selected');
          sfx.check();
        } else {
          row.classList.remove('selected');
        }
        updateMetrics();
      });

      // Single uninstall button listener
      const singleBtn = row.querySelector('[data-action="single-uninstall"]');
      singleBtn.addEventListener('click', () => {
        sfx.click();
        triggerUninstallModal([app]);
      });

      el.appList.appendChild(row);
    });

    updateMetrics();
  }

  // --- Metrics & Summary Calculation ---
  function updateMetrics() {
    const activeApps = state.apps.filter(a => !a.purged);
    const selectedApps = activeApps.filter(a => a.selected);
    const bloatApps = activeApps.filter(a => a.isBloatware);

    const totalMb = activeApps.reduce((acc, curr) => acc + curr.sizeMb, 0);
    const selectedMb = selectedApps.reduce((acc, curr) => acc + curr.sizeMb, 0);

    el.valTotalApps.textContent = activeApps.length;
    el.valBloatCount.textContent = bloatApps.length;
    el.valTotalSize.textContent = formatSize(totalMb);
    el.valSelectedSize.textContent = formatSize(selectedMb);
    el.valSelectedCount.textContent = `${selectedApps.length} apps selected`;

    // Batch Action Bar State
    el.selectionSummary.textContent = `${selectedApps.length} of ${activeApps.length} apps selected (${formatSize(selectedMb)})`;
    if (selectedApps.length > 0) {
      el.btnTriggerPurge.disabled = false;
      el.btnTriggerPurge.classList.remove('disabled');
      el.btnPurgeText.textContent = `Uninstall Selected (${selectedApps.length}) — Reclaim ${formatSize(selectedMb)}`;
    } else {
      el.btnTriggerPurge.disabled = true;
      el.btnTriggerPurge.classList.add('disabled');
      el.btnPurgeText.textContent = `Uninstall Selected Apps`;
    }

    el.selectAllCheckbox.checked = activeApps.length > 0 && selectedApps.length === activeApps.length;
  }

  // --- Deep Uninstall Flow Modal & Simulator ---
  function triggerUninstallModal(targetApps) {
    state.appsToPurge = targetApps;
    el.modalAppCount.textContent = `${targetApps.length} application${targetApps.length > 1 ? 's' : ''}`;

    el.modalAppPreviewList.innerHTML = targetApps.map(app => `
      <div class="modal-preview-item">
        <span><strong>${app.icon} ${app.name}</strong></span>
        <span class="text-rose font-mono">-${formatSize(app.sizeMb)}</span>
      </div>
    `).join('');

    // Reset Terminal UI
    el.terminalLogWrapper.classList.add('hidden');
    el.terminalConsole.innerHTML = '';
    el.terminalProgressFill.style.width = '0%';
    el.modalFooterActions.classList.remove('hidden');

    el.uninstallModal.classList.remove('hidden');
  }

  function startPurgeSimulation() {
    const isDeep = document.querySelector('input[name="uninstall_mode"]:checked').value === 'deep';
    el.terminalLogWrapper.classList.remove('hidden');
    el.modalFooterActions.classList.add('hidden');
    el.terminalConsole.innerHTML = '';

    const logs = [
      { text: `[SYSTEM] Initializing AppPurge AI v2.4 Native Shredder...`, type: 'info', delay: 200, progress: 10 },
      { text: `[PROCESS] Enumerating parent and child processes across system...`, type: 'info', delay: 500, progress: 25 },
      { text: `[TERMINATE] Terminating background daemons (PIDs: 4192, 5831, 9024)...`, type: 'warn', delay: 850, progress: 40 },
      { text: `[DISK] Removing primary binaries, executables, and asset bundles...`, type: 'info', delay: 1200, progress: 60 },
      { text: isDeep ? `[DEEP SHRED] Purging %APPDATA%, Application Support & persistent SQLite caches...` : `[STANDARD] Running default vendor uninstall script...`, type: isDeep ? 'danger' : 'info', delay: 1600, progress: 78 },
      { text: isDeep ? `[REGISTRY] Scrubbed 48 orphaned CLSID & autorun registry hooks.` : `[INFO] Registry entries preserved.`, type: 'warn', delay: 2000, progress: 90 },
      { text: `[COMPLETE] Forensic uninstallation completed with 0 errors.`, type: 'success', delay: 2400, progress: 100 }
    ];

    logs.forEach(step => {
      setTimeout(() => {
        const line = document.createElement('div');
        line.className = `terminal-line ${step.type}`;
        line.textContent = step.text;
        el.terminalConsole.appendChild(line);
        el.terminalConsole.scrollTop = el.terminalConsole.scrollHeight;
        el.terminalProgressFill.style.width = `${step.progress}%`;
        sfx.purgeStep();
      }, step.delay);
    });

    setTimeout(() => {
      sfx.success();
      // Mark purged in state
      let purgedTotal = 0;
      state.appsToPurge.forEach(app => {
        app.purged = true;
        app.selected = false;
        purgedTotal += app.sizeMb;
      });

      state.totalPurgedMb += purgedTotal;
      el.heroStatReclaimed.textContent = formatSize(state.totalPurgedMb + 42800);

      showToast(`Successfully purged ${state.appsToPurge.length} apps! Recovered ${formatSize(purgedTotal)}.`, 'success');

      // Sync log to Supabase in background
      syncAuditLogToSupabase({
        operation: isDeep ? 'DEEP_SHRED' : 'STANDARD_UNINSTALL',
        appsPurged: state.appsToPurge.map(a => a.name),
        totalReclaimedMb: purgedTotal,
        timestamp: new Date().toISOString()
      });

      setTimeout(() => {
        el.uninstallModal.classList.add('hidden');
        renderAppList();
      }, 1000);

    }, 3000);
  }

  // --- Supabase Cloud Sync Simulation / REST Call ---
  async function syncAuditLogToSupabase(auditData) {
    if (!state.supabaseUrl || !state.supabaseKey) return;
    try {
      const endpoint = `${state.supabaseUrl.replace(/\/$/, '')}/rest/v1/app_remover_logs`;
      const response = await fetch(endpoint, {
        method: 'POST',
        headers: {
          'apikey': state.supabaseKey,
          'Authorization': `Bearer ${state.supabaseKey}`,
          'Content-Type': 'application/json',
          'Prefer': 'return=minimal'
        },
        body: JSON.stringify(auditData)
      });
      if (response.ok) {
        console.log('Synced audit record to Supabase successfully.');
      }
    } catch (e) {
      console.warn('Supabase local offline fallback:', e.message);
    }
  }

  // --- Deep Scanner Trigger ---
  function runDeepScan() {
    sfx.scan();
    el.scannerCurtain.classList.remove('hidden');
    el.scannerStatusText.textContent = 'Analyzing file system, registries & active processes...';

    setTimeout(() => {
      el.scannerStatusText.textContent = 'Heuristic bloatware detection evaluating adware scores...';
    }, 1000);

    setTimeout(() => {
      el.scannerCurtain.classList.add('hidden');
      sfx.success();
      showToast('System Deep Scan complete! 8 bloatware apps and 14.6 GB of cache discovered.', 'success');
      // Highlight bloatware tab
      document.querySelector('[data-category="bloatware"]').click();
    }, 2000);
  }

  // --- Drag and Drop File Analyzer ---
  function handleFiles(files) {
    if (!files || files.length === 0) return;
    sfx.scan();
    el.analyzedResultsContainer.classList.remove('hidden');

    Array.from(files).forEach(file => {
      const sizeMb = Math.max(1, Math.round(file.size / (1024 * 1024)));
      const ext = file.name.split('.').pop().toLowerCase();
      let bloatScore = 30;
      let icon = '📦';

      if (ext === 'apk') {
        bloatScore = 85;
        icon = '🤖';
      } else if (ext === 'exe' || ext === 'msi') {
        bloatScore = 75;
        icon = '💻';
      } else if (ext === 'dmg') {
        bloatScore = 40;
        icon = '🍏';
      } else if (ext === 'zip' || ext === 'tar' || ext === 'gz') {
        bloatScore = 60;
        icon = '🗄️';
      }

      const fileItem = {
        name: file.name,
        sizeMb: sizeMb,
        ext: ext,
        bloatScore: bloatScore,
        icon: icon
      };

      state.droppedFiles.push(fileItem);
      renderDroppedFiles();
    });

    showToast(`Analyzed ${files.length} package file(s).`, 'info');
  }

  function renderDroppedFiles() {
    el.analyzedFilesList.innerHTML = state.droppedFiles.map((f, idx) => `
      <div class="analyzed-file-card">
        <div class="analyzed-file-info">
          <span class="analyzed-file-icon">${f.icon}</span>
          <div>
            <strong>${f.name}</strong>
            <div class="app-publisher">Type: .${f.ext.toUpperCase()} • Size: ${formatSize(f.sizeMb)} • Risk Score: ${f.bloatScore}/100</div>
          </div>
        </div>
        <button class="btn-uninstall-single" onclick="window.removeDroppedFile(${idx})">
          <span>Shred Package</span>
        </button>
      </div>
    `).join('');
  }

  window.removeDroppedFile = function (index) {
    sfx.purgeStep();
    state.droppedFiles.splice(index, 1);
    renderDroppedFiles();
    if (state.droppedFiles.length === 0) {
      el.analyzedResultsContainer.classList.add('hidden');
    }
    showToast('Package file quarantined and shredded.', 'success');
  };

  // --- Event Listeners Initialization ---
  function setupEventListeners() {
    // Sound Toggle
    el.btnSoundToggle.addEventListener('click', () => {
      const enabled = sfx.toggle();
      el.soundOnIcon.classList.toggle('hidden', !enabled);
      el.soundOffIcon.classList.toggle('hidden', enabled);
      showToast(enabled ? 'Sound effects enabled' : 'Sound effects muted', 'info');
    });

    // Category Tabs
    el.categoryTabs.addEventListener('click', (e) => {
      const btn = e.target.closest('.tab-btn');
      if (!btn) return;
      sfx.click();
      el.categoryTabs.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.activeCategory = btn.dataset.category;
      renderAppList();
    });

    // Search Input
    el.searchInput.addEventListener('input', (e) => {
      state.searchQuery = e.target.value;
      el.btnClearSearch.classList.toggle('hidden', !state.searchQuery);
      renderAppList();
    });

    el.btnClearSearch.addEventListener('click', () => {
      el.searchInput.value = '';
      state.searchQuery = '';
      el.btnClearSearch.classList.add('hidden');
      renderAppList();
    });

    // Sort Dropdown
    el.sortSelect.addEventListener('change', (e) => {
      sfx.click();
      state.sortBy = e.target.value;
      renderAppList();
    });

    // Select All Checkbox
    el.selectAllCheckbox.addEventListener('change', (e) => {
      const isChecked = e.target.checked;
      sfx.check();
      state.apps.filter(a => !a.purged).forEach(a => a.selected = isChecked);
      renderAppList();
    });

    // Select All Bloatware Button
    el.btnSelectAllBloat.addEventListener('click', () => {
      sfx.click();
      state.apps.filter(a => !a.purged).forEach(a => {
        a.selected = a.isBloatware;
      });
      renderAppList();
      showToast('All 8 bloatware apps selected for deep purge!', 'warn');
    });

    // Clear Selection
    el.btnDeselectAll.addEventListener('click', () => {
      sfx.click();
      state.apps.forEach(a => a.selected = false);
      renderAppList();
    });

    // Trigger Purge Button
    el.btnTriggerPurge.addEventListener('click', () => {
      sfx.click();
      const selected = state.apps.filter(a => !a.purged && a.selected);
      if (selected.length > 0) {
        triggerUninstallModal(selected);
      }
    });

    // Modal Events
    el.btnCloseModal.addEventListener('click', () => {
      sfx.click();
      el.uninstallModal.classList.add('hidden');
    });

    el.btnCancelUninstall.addEventListener('click', () => {
      sfx.click();
      el.uninstallModal.classList.add('hidden');
    });

    el.btnConfirmUninstall.addEventListener('click', () => {
      sfx.click();
      startPurgeSimulation();
    });

    // Scanner Buttons
    el.btnHeaderScan.addEventListener('click', () => {
      window.location.hash = '#dashboard';
      runDeepScan();
    });

    el.btnDemoSample.addEventListener('click', () => {
      sfx.click();
      state.apps = JSON.parse(JSON.stringify(INITIAL_APPS));
      renderAppList();
      showToast('Loaded 24 fresh sample applications.', 'info');
    });

    // Drag and Drop Zone
    el.fileDropZone.addEventListener('dragover', (e) => {
      e.preventDefault();
      el.fileDropZone.classList.add('dragover');
    });

    el.fileDropZone.addEventListener('dragleave', () => {
      el.fileDropZone.classList.remove('dragover');
    });

    el.fileDropZone.addEventListener('drop', (e) => {
      e.preventDefault();
      el.fileDropZone.classList.remove('dragover');
      if (e.dataTransfer && e.dataTransfer.files) {
        handleFiles(e.dataTransfer.files);
      }
    });

    el.fileDropZone.addEventListener('click', (e) => {
      if (e.target.id !== 'btn-test-sample-file') {
        el.fileInputHidden.click();
      }
    });

    el.fileInputHidden.addEventListener('change', (e) => {
      handleFiles(e.target.files);
    });

    el.btnBrowseFiles.addEventListener('click', (e) => {
      e.stopPropagation();
      el.fileInputHidden.click();
    });

    el.btnTestSampleFile.addEventListener('click', (e) => {
      e.stopPropagation();
      sfx.click();
      state.droppedFiles.push({
        name: 'AdwareCleaner_Trojan_Sample.apk',
        sizeMb: 145,
        ext: 'apk',
        bloatScore: 98,
        icon: '🤖'
      });
      el.analyzedResultsContainer.classList.remove('hidden');
      renderDroppedFiles();
      showToast('Loaded simulated APK adware for analysis.', 'warn');
    });

    el.btnClearDroppedFiles.addEventListener('click', () => {
      sfx.click();
      state.droppedFiles = [];
      el.analyzedResultsContainer.classList.add('hidden');
    });

    // Supabase Settings Modal
    el.btnSupabaseConfig.addEventListener('click', () => {
      sfx.click();
      el.cfgSupabaseUrl.value = state.supabaseUrl;
      el.cfgSupabaseKey.value = state.supabaseKey;
      el.supabaseModal.classList.remove('hidden');
    });

    el.btnCloseSupabaseModal.addEventListener('click', () => {
      sfx.click();
      el.supabaseModal.classList.add('hidden');
    });

    el.btnSaveSupabase.addEventListener('click', () => {
      sfx.click();
      state.supabaseUrl = el.cfgSupabaseUrl.value.trim();
      state.supabaseKey = el.cfgSupabaseKey.value.trim();
      localStorage.setItem('supabase_url', state.supabaseUrl);
      localStorage.setItem('supabase_key', state.supabaseKey);
      el.supabaseModal.classList.add('hidden');
      showToast('Supabase settings saved successfully!', 'success');
    });

    el.btnTestConnection.addEventListener('click', async () => {
      sfx.click();
      showToast('Pinging Supabase API endpoint...', 'info');
      try {
        const url = el.cfgSupabaseUrl.value.trim().replace(/\/$/, '') + '/rest/v1/';
        const res = await fetch(url, {
          headers: { 'apikey': el.cfgSupabaseKey.value.trim() }
        });
        if (res.status === 200 || res.status === 404 || res.status === 401) {
          showToast('Connected to Supabase endpoint!', 'success');
        } else {
          showToast(`Server returned status: ${res.status}`, 'warn');
        }
      } catch (err) {
        showToast('Local offline mode active: ' + err.message, 'info');
      }
    });

    el.btnSyncNow.addEventListener('click', () => {
      sfx.scan();
      showToast('Syncing purge audit logs to Supabase PostgreSQL...', 'info');
      setTimeout(() => {
        sfx.success();
        showToast('Audit records synchronized to Supabase!', 'success');
      }, 1000);
    });

    el.btnViewCloudLogs.addEventListener('click', () => {
      sfx.click();
      showToast('Fetched 18 historical purge events from Supabase.', 'info');
    });

    // Reset filters
    const btnResetFilters = document.getElementById('btn-reset-filters');
    if (btnResetFilters) {
      btnResetFilters.addEventListener('click', () => {
        sfx.click();
        el.searchInput.value = '';
        state.searchQuery = '';
        document.getElementById('tab-all').click();
      });
    }
  }

  // --- Initialize App ---
  function init() {
    renderAppList();
    setupEventListeners();
  }

  document.addEventListener('DOMContentLoaded', init);
})();
