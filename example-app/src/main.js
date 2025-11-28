import './style.css';
import { CapgoCompass } from '@capgo/capacitor-compass';

const plugin = CapgoCompass;
const state = {
  listenerHandle: null,
};

const actions = [
  {
    id: 'get-current-heading',
    label: 'Get current heading',
    description: 'Calls getCurrentHeading() to get the current compass heading in degrees (0-360).',
    inputs: [],
    run: async () => {
      const heading = await plugin.getCurrentHeading();
      return heading;
    },
  },
  {
    id: 'start-listening',
    label: 'Start listening',
    description: 'Starts listening for compass heading changes. Updates will appear in the output below.',
    inputs: [],
    run: async () => {
      if (state.listenerHandle) {
        return { error: 'Already listening. Stop listening first.' };
      }

      state.listenerHandle = await plugin.addListener('headingChange', (event) => {
        const output = document.getElementById('plugin-output');
        output.textContent = JSON.stringify(event, null, 2);
      });

      await plugin.startListening();
      return { message: 'Listening started' };
    },
  },
  {
    id: 'stop-listening',
    label: 'Stop listening',
    description: 'Stops listening for compass heading changes.',
    inputs: [],
    run: async () => {
      if (!state.listenerHandle) {
        return { error: 'Not currently listening.' };
      }

      await plugin.stopListening();
      await state.listenerHandle.remove();
      state.listenerHandle = null;
      return { message: 'Listening stopped' };
    },
  },
  {
    id: 'get-plugin-version',
    label: 'Get plugin version',
    description: 'Returns the native plugin version.',
    inputs: [],
    run: async () => {
      const version = await plugin.getPluginVersion();
      return version;
    },
  },
  {
    id: 'check-permissions',
    label: 'Check permissions',
    description: 'Checks the current permission status for compass access.',
    inputs: [],
    run: async () => {
      const status = await plugin.checkPermissions();
      return status;
    },
  },
  {
    id: 'request-permissions',
    label: 'Request permissions',
    description: 'Requests permission to access compass data (iOS requires location permission).',
    inputs: [],
    run: async () => {
      const status = await plugin.requestPermissions();
      return status;
    },
  },
];

const actionSelect = document.getElementById('action-select');
const formContainer = document.getElementById('action-form');
const descriptionBox = document.getElementById('action-description');
const runButton = document.getElementById('run-action');
const output = document.getElementById('plugin-output');

function buildForm(action) {
  formContainer.innerHTML = '';
  if (!action.inputs || !action.inputs.length) {
    const note = document.createElement('p');
    note.className = 'no-input-note';
    note.textContent = 'This action does not require any inputs.';
    formContainer.appendChild(note);
    return;
  }
  action.inputs.forEach((input) => {
    const fieldWrapper = document.createElement('div');
    fieldWrapper.className = input.type === 'checkbox' ? 'form-field inline' : 'form-field';

    const label = document.createElement('label');
    label.textContent = input.label;
    label.htmlFor = `field-${input.name}`;

    let field;
    switch (input.type) {
      case 'textarea': {
        field = document.createElement('textarea');
        field.rows = input.rows || 4;
        break;
      }
      case 'select': {
        field = document.createElement('select');
        (input.options || []).forEach((option) => {
          const opt = document.createElement('option');
          opt.value = option.value;
          opt.textContent = option.label;
          if (input.value !== undefined && option.value === input.value) {
            opt.selected = true;
          }
          field.appendChild(opt);
        });
        break;
      }
      case 'checkbox': {
        field = document.createElement('input');
        field.type = 'checkbox';
        field.checked = Boolean(input.value);
        break;
      }
      case 'number': {
        field = document.createElement('input');
        field.type = 'number';
        if (input.value !== undefined && input.value !== null) {
          field.value = String(input.value);
        }
        break;
      }
      default: {
        field = document.createElement('input');
        field.type = 'text';
        if (input.value !== undefined && input.value !== null) {
          field.value = String(input.value);
        }
      }
    }

    field.id = `field-${input.name}`;
    field.name = input.name;
    field.dataset.type = input.type || 'text';

    if (input.placeholder && input.type !== 'checkbox') {
      field.placeholder = input.placeholder;
    }

    if (input.type === 'checkbox') {
      fieldWrapper.appendChild(field);
      fieldWrapper.appendChild(label);
    } else {
      fieldWrapper.appendChild(label);
      fieldWrapper.appendChild(field);
    }

    formContainer.appendChild(fieldWrapper);
  });
}

function getFormValues(action) {
  const values = {};
  (action.inputs || []).forEach((input) => {
    const field = document.getElementById(`field-${input.name}`);
    if (!field) return;
    switch (input.type) {
      case 'number': {
        values[input.name] = field.value === '' ? null : Number(field.value);
        break;
      }
      case 'checkbox': {
        values[input.name] = field.checked;
        break;
      }
      default: {
        values[input.name] = field.value;
      }
    }
  });
  return values;
}

function setAction(action) {
  descriptionBox.textContent = action.description || '';
  buildForm(action);
  output.textContent = 'Ready to run the selected action.';
}

function populateActions() {
  actionSelect.innerHTML = '';
  actions.forEach((action) => {
    const option = document.createElement('option');
    option.value = action.id;
    option.textContent = action.label;
    actionSelect.appendChild(option);
  });
  setAction(actions[0]);
}

actionSelect.addEventListener('change', () => {
  const action = actions.find((item) => item.id === actionSelect.value);
  if (action) {
    setAction(action);
  }
});

runButton.addEventListener('click', async () => {
  const action = actions.find((item) => item.id === actionSelect.value);
  if (!action) return;
  const values = getFormValues(action);
  try {
    const result = await action.run(values);
    if (result === undefined) {
      output.textContent = 'Action completed.';
    } else if (typeof result === 'string') {
      output.textContent = result;
    } else {
      output.textContent = JSON.stringify(result, null, 2);
    }
  } catch (error) {
    output.textContent = `Error: ${error?.message ?? error}`;
  }
});

populateActions();
