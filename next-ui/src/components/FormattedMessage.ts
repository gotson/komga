import { defineComponent, h } from 'vue'
import { type MessageDescriptor, useIntl } from 'vue-intl'

// from: https://github.com/formatjs/formatjs/discussions/3961
export default defineComponent({
  props: {
    messageDescriptor: Object as PropType<MessageDescriptor>,
    tag: String,
    values: Object,
  },
  setup(props, context) {
    const { messageDescriptor, tag, values = {} } = props
    const intl = useIntl()
    const slotNames = Object.keys(context.slots)

    const message = intl.formatMessage(messageDescriptor, {
      ...values,
      ...slotNames.reduce((slots, name) => {
        slots[name] = (content) => context.slots[name](() => content)
        return slots
      }, {}),
    })

    return () => (tag || Array.isArray(message) ? h(tag || 'div', message) : message)
  },
})
